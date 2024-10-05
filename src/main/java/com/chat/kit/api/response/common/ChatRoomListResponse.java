package com.chat.kit.api.response.common;

import com.chat.kit.persistence.domain.ChatMessage;
import com.chat.kit.persistence.domain.MemberChatRoom;
import com.chat.kit.persistence.domain.MessageReadStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatRoomListResponse {
    private Long roomId;
    private List<Long> memberIds;
    private MessageReadStatus messageReadStatus;
    private String lastMsg;

    public static ChatRoomListResponse of(Long roomId, List<Long> memberIds, MessageReadStatus messageReadStatus, String lastMsg){
        return ChatRoomListResponse.builder()
                .roomId(roomId)
                .memberIds(memberIds)
                .messageReadStatus(messageReadStatus)
                .lastMsg(lastMsg)
                .build();
    }
}
