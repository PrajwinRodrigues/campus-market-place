package com.campusmarket.service;

import com.campusmarket.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/png", "image/jpeg", "image/jpg", "image/webp");
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5MB

    private final Path uploadRoot;
    private final String cloudinaryName;
    private final String cloudinaryKey;
    private final String cloudinarySecret;
    private final boolean cloudinaryRequired;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FileStorageService(@Value("${app.upload.dir}") String uploadDir,
                              @Value("${app.cloudinary.cloud-name:}") String cloudinaryName,
                              @Value("${app.cloudinary.api-key:}") String cloudinaryKey,
                              @Value("${app.cloudinary.api-secret:}") String cloudinarySecret,
                              @Value("${app.cloudinary.required:false}") boolean cloudinaryRequired) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.cloudinaryName = cloudinaryName;
        this.cloudinaryKey = cloudinaryKey;
        this.cloudinarySecret = cloudinarySecret;
        this.cloudinaryRequired = cloudinaryRequired;
        boolean anyCloudinarySetting = !cloudinaryName.isBlank() || !cloudinaryKey.isBlank() || !cloudinarySecret.isBlank();
        if ((cloudinaryRequired || anyCloudinarySetting) && !cloudinaryConfigured()) {
            throw new IllegalStateException("Set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET together");
        }
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory: " + uploadRoot, e);
        }
    }

    public List<String> storeAll(List<MultipartFile> files) {
        return files.stream().map(this::store).toList();
    }

    /**
     * Saves an uploaded image and returns the public URL path to fetch it back
     * (served statically from /uploads/** — see WebConfig).
     */
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("File exceeds the 5MB size limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Only PNG, JPEG, or WEBP images are allowed");
        }

        if (cloudinaryConfigured()) {
            return storeInCloudinary(file);
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";
        String storedFilename = UUID.randomUUID() + extension;

        Path target = uploadRoot.resolve(storedFilename).normalize();
        if (!target.getParent().equals(uploadRoot)) {
            throw new BadRequestException("Invalid file path");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file", e);
        }

        return "/uploads/" + storedFilename;
    }

    private boolean cloudinaryConfigured() {
        return !cloudinaryName.isBlank() && !cloudinaryKey.isBlank() && !cloudinarySecret.isBlank();
    }

    private String storeInCloudinary(MultipartFile file) {
        long timestamp = Instant.now().getEpochSecond();
        String signature = sha1("timestamp=" + timestamp + cloudinarySecret);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(readBytes(file)) {
            @Override
            public String getFilename() {
                String extension = switch (file.getContentType().toLowerCase()) {
                    case "image/png" -> ".png";
                    case "image/webp" -> ".webp";
                    default -> ".jpg";
                };
                return "campus-marketplace-upload" + extension;
            }
        });
        body.add("api_key", cloudinaryKey);
        body.add("timestamp", Long.toString(timestamp));
        body.add("signature", signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        String endpoint = "https://api.cloudinary.com/v1_1/" + cloudinaryName + "/image/upload";
        try {
            String response = restTemplate.exchange(endpoint, HttpMethod.POST,
                    new HttpEntity<>(body, headers), String.class).getBody();
            JsonNode payload = objectMapper.readTree(response == null ? "{}" : response);
            String secureUrl = payload.path("secure_url").asText();
            if (secureUrl.isBlank()) {
                throw new IllegalStateException("Image storage did not return a secure URL");
            }
            return secureUrl;
        } catch (Exception e) {
            throw new IllegalStateException("Could not store image in Cloudinary", e);
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read uploaded image", e);
        }
    }

    private static String sha1(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Could not sign image upload", e);
        }
    }
}
