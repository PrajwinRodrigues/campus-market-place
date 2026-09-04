package com.campusmarket.service;

import com.campusmarket.dto.message.ConversationSummary;
import com.campusmarket.dto.message.MessageRequest;
import com.campusmarket.dto.message.MessageResponse;
import com.campusmarket.entity.Listing;
import com.campusmarket.entity.Message;
import com.campusmarket.entity.User;
import com.campusmarket.exception.BadRequestException;
import com.campusmarket.exception.ResourceNotFoundException;
import com.campusmarket.repository.ListingRepository;
import com.campusmarket.repository.MessageRepository;
import com.campusmarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageResponse send(MessageRequest request, User sender) {
        if (request.receiverId().equals(sender.getId())) {
            throw new BadRequestException("You cannot message yourself");
        }
        Listing listing = listingRepository.findById(request.listingId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + request.listingId()));
        User receiver = userRepository.findById(request.receiverId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.receiverId()));

        Message message = Message.builder()
                .listing(listing)
                .sender(sender)
                .receiver(receiver)
                .content(request.content())
                .build();

        return MessageResponse.from(messageRepository.save(message));
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getConversation(Long listingId, Long otherUserId, User currentUser) {
        List<Message> messages = messageRepository.findConversation(listingId, currentUser.getId(), otherUserId);
        return messages.stream().map(MessageResponse::from).toList();
    }

    @Transactional
    public void markConversationRead(Long listingId, Long otherUserId, User currentUser) {
        List<Message> messages = messageRepository.findConversation(listingId, currentUser.getId(), otherUserId);
        for (Message m : messages) {
            if (m.getReceiver().getId().equals(currentUser.getId()) && !m.isRead()) {
                m.setRead(true);
            }
        }
    }

    /**
     * One row per (listing, other participant) pair the current user has
     * exchanged messages in, most recently active first.
     */
    @Transactional(readOnly = true)
    public List<ConversationSummary> getMyConversations(User currentUser) {
        List<Message> all = messageRepository.findAllInvolvingUser(currentUser.getId());

        Map<String, Message> lastMessageByThread = new LinkedHashMap<>();
        Map<String, Long> unreadByThread = new LinkedHashMap<>();

        for (Message m : all) {
            User other = m.getSender().getId().equals(currentUser.getId()) ? m.getReceiver() : m.getSender();
            String key = m.getListing().getId() + ":" + other.getId();

            lastMessageByThread.putIfAbsent(key, m); // list is already newest-first
            if (m.getReceiver().getId().equals(currentUser.getId()) && !m.isRead()) {
                unreadByThread.merge(key, 1L, Long::sum);
            } else {
                unreadByThread.putIfAbsent(key, 0L);
            }
        }

        return lastMessageByThread.entrySet().stream()
                .map(entry -> {
                    Message m = entry.getValue();
                    User other = m.getSender().getId().equals(currentUser.getId()) ? m.getReceiver() : m.getSender();
                    return new ConversationSummary(
                            m.getListing().getId(),
                            m.getListing().getTitle(),
                            other.getId(),
                            other.getFullName(),
                            m.getContent(),
                            m.getSentAt(),
                            unreadByThread.getOrDefault(entry.getKey(), 0L)
                    );
                })
                .sorted(Comparator.comparing(ConversationSummary::lastMessageAt).reversed())
                .toList();
    }
}
