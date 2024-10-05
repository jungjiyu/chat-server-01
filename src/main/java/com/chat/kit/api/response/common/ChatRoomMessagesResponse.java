package com.chat.kit.api.response.common;

import com.chat.kit.persistence.domain.ChatMessage;
import com.chat.kit.persistence.domain.ChatType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatRoomMessagesResponse {
    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String message;
    private LocalDateTime createdAt;
    private ChatType chatType;

    public static ChatRoomMessagesResponse of(ChatMessage chatMessage){
        return ChatRoomMessagesResponse.builder()
                .messageId(chatMessage.getId())
                .senderId(chatMessage.getMemberId())
                .roomId(chatMessage.getChatRoom().getId())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getSentAt())
                .chatType(chatMessage.getChatType())
                .build();
    }
}
