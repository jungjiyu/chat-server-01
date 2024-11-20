package com.chat.kit.api.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class FindChatRoomDto {
    private List<Long> memberIds;
}
