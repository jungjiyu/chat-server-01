package com.chat.kit.api.response.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindChatRoomResponse {
    private Long roomId;
    public static FindChatRoomResponse of(Long roomId){
        return FindChatRoomResponse.builder()
                .roomId(roomId)
                .build();
    }
}
