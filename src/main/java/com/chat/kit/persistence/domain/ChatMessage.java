package com.chat.kit.persistence.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    @Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
    @Column(name = "CHAT_MESSAGE_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CHAT_ROOM_ID")
    private ChatRoom chatRoom;

    //Member객체와 1대1 연관관계를 맺을 경우, 자동으로 unique 제약조건이 걸림
    //이는 한 방에서 한 회원은 한 번만 채팅을 남길 수 있다는 의미
    @Column(name = "MEMBER_ID")
    private Long memberId;

    private String message;

    @Enumerated(EnumType.STRING)
    private ChatType chatType;

    private LocalDateTime sentAt;

    @Builder
    public ChatMessage(ChatRoom chatRoom, Long memberId, String message, ChatType chatType, LocalDateTime sentAt) {
        this.chatRoom = chatRoom;
        this.memberId = memberId;
        this.message = message;
        this.chatType = chatType;
        this.sentAt = sentAt;
    }
}
