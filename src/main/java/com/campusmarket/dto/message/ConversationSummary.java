package com.campusmarket.dto.message;

import java.time.Instant;

public record ConversationSummary(
        Long listingId,
        String listingTitle,
        Long otherUserId,
        String otherUserName,
        String lastMessage,
        Instant lastMessageAt,
        long unreadCount
) {
}
