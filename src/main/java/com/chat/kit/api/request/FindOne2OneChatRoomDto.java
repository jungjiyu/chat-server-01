package com.chat.kit.api.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class FindOne2OneChatRoomDto {
    private List<Long> memberIds;
}
