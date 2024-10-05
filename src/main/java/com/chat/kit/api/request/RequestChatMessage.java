package com.chat.kit.api.request;

import com.chat.kit.persistence.domain.ChatType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Getter
@NoArgsConstructor
public class RequestChatMessage {

    private Long senderId;
    private Long roomId;
    private String message;
    private ChatType chatType;

    public RequestChatMessage(Long senderId, Long roomId, String message, ChatType chatType) {
        this.senderId = senderId;
        this.roomId = roomId;
        this.message = message;
        this.chatType = chatType;
    }
}