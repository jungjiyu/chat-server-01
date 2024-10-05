package com.chat.kit.persistence.repository;

import com.chat.kit.persistence.domain.ChatMessage;
import com.chat.kit.persistence.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBychatRoom(ChatRoom chatRoom);
    @Query("SELECT cm from ChatMessage cm left " +
            "join ChatRoom cr on cr.id=cm.chatRoom.id " +
            "left join MemberChatRoom mc on mc.chatRoom.id=cr.id and mc.member.id=:memberId " +
            "where cm.sentAt > mc.lastLeavedTime"
    )
    List<ChatMessage> findUnReadMsgByMemberId(Long memberId);

    @Query("SELECT cm from ChatMessage cm where cm.chatRoom.id=:roomId order by cm.sentAt desc limit 1")
    Optional<ChatMessage> findLastMsgByChatRoomId(Long roomId);
}
