package com.chat.kit.persistence.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberChatRoom {
    @Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
    @Column(name = "MEMBER_CHAR_ROOM_ID")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    private Member member;
    @ManyToOne
    @JoinColumn(name = "CHAT_ROOM_ID")
    private ChatRoom chatRoom;

    @Builder.Default
    private LocalDateTime lastLeavedTime = LocalDateTime.now();

}
