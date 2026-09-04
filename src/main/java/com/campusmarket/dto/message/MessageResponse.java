package com.campusmarket.dto.message;

import com.campusmarket.entity.Message;

import java.time.Instant;

public record MessageResponse(
        Long id,
        Long listingId,
        String listingTitle,
        Long senderId,
        String senderName,
        Long receiverId,
        String receiverName,
        String content,
        Instant sentAt,
        boolean read
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getListing().getId(),
                message.getListing().getTitle(),
                message.getSender().getId(),
                message.getSender().getFullName(),
                message.getReceiver().getId(),
                message.getReceiver().getFullName(),
                message.getContent(),
                message.getSentAt(),
                message.isRead()
        );
    }
}
