package com.campusmarket.service;

import com.campusmarket.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/png", "image/jpeg", "image/jpg", "image/webp");
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5MB

    private final Path uploadRoot;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
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
}
