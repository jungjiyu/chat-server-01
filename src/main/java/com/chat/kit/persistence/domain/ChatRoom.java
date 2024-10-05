package com.chat.kit.persistence.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ChatRoom{
    @Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
    @Column(name = "CHAT_ROOM_ID")
    private Long id;
    @OneToMany(mappedBy = "chatRoom")
    private List<MemberChatRoom> memberChatRoomOne = new ArrayList<>();
    @Enumerated(EnumType.STRING)
    @Column(name = "ROOM_TYPE")
    private RoomType roomType;

    @OneToMany(mappedBy = "chatRoom")
    private List<ChatMessage> chatMessages= new ArrayList<>();

    @Builder
    public ChatRoom(RoomType roomType) {
        this.roomType = roomType;
    }
}
