package com.campusmarket.controller;

import com.campusmarket.dto.message.ConversationSummary;
import com.campusmarket.dto.message.MessageRequest;
import com.campusmarket.dto.message.MessageResponse;
import com.campusmarket.entity.User;
import com.campusmarket.service.MessageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messages")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponse> send(@Valid @RequestBody MessageRequest request,
                                                  @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(messageService.send(request, currentUser));
    }

    @GetMapping("/conversations")
    public List<ConversationSummary> myConversations(@AuthenticationPrincipal User currentUser) {
        return messageService.getMyConversations(currentUser);
    }

    @GetMapping("/listing/{listingId}/user/{otherUserId}")
    public List<MessageResponse> conversation(@PathVariable Long listingId,
                                               @PathVariable Long otherUserId,
                                               @AuthenticationPrincipal User currentUser) {
        return messageService.getConversation(listingId, otherUserId, currentUser);
    }

    @PatchMapping("/listing/{listingId}/user/{otherUserId}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long listingId,
                                          @PathVariable Long otherUserId,
                                          @AuthenticationPrincipal User currentUser) {
        messageService.markConversationRead(listingId, otherUserId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
