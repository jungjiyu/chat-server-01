package com.chat.kit.persistence.repository;

import com.chat.kit.persistence.domain.ChatRoom;
import com.chat.kit.persistence.domain.Member;
import com.chat.kit.persistence.domain.MemberChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface MemberChatRoomRepository extends JpaRepository<MemberChatRoom,Long> {
    List<MemberChatRoom> findByMember(Member member);
    @Query("SELECT memberChatRoom from MemberChatRoom memberChatRoom where memberChatRoom.member.id = :memberId")
    List<MemberChatRoom> findByMemberId(Long memberId);
    @Query("SELECT memberChatRoom from MemberChatRoom memberChatRoom where memberChatRoom.chatRoom.id = :id")
    List<MemberChatRoom> findByChatRoomId(Long id);
    @Query("SELECT memberChatRoom from MemberChatRoom memberChatRoom where memberChatRoom.member.id = :memberId and memberChatRoom.chatRoom.id = :chatRoomId")
    Optional<MemberChatRoom> findByChatRoomIdAndMemberId(Long chatRoomId, Long memberId);
    List<MemberChatRoom> findByChatRoom(ChatRoom chatRoom);
    @Query("SELECT memberChatRoom from MemberChatRoom memberChatRoom where memberChatRoom.chatRoom.id in :chatRoomIds")
    List<MemberChatRoom> findChatListIn(List<Long> chatRoomIds);
    @Query(value = "select memberChatRoom.lastLeavedTime from MemberChatRoom memberChatRoom WHERE memberChatRoom.member = :member AND memberChatRoom.chatRoom = :chatRoom")
    LocalDateTime lastLeavedTime(@Param("member") Member member, @Param("chatRoom") ChatRoom chatRoom);
    @Query(value = "select memberChatRoom " +
            "from MemberChatRoom memberChatRoom " +
            "where memberChatRoom.member = :member1 " +
            "or memberChatRoom.member = :member2" )
    List<MemberChatRoom> findByTwoMember(@Param("member1") Member member1, @Param("member2") Member member2);

    @Query(value = "select COUNT(*), memberChatRoom.chatRoom " +
            "from MemberChatRoom memberChatRoom " +
            "where memberChatRoom.member = :member1 " +
            "or memberChatRoom.member = :member2 GROUP BY memberChatRoom.chatRoom")
    List<Object[]> findByTwoMemberGroup(@Param("member1") Member member1, @Param("member2") Member member2);

    @Query(value = "SELECT CHAT_ROOM_ID,  MEMBER_NUM FROM (SELECT CHAT_ROOM_ID, COUNT(*) AS MEMBER_NUM FROM MEMBER_CHAT_ROOM WHERE MEMBER_ID = :member1Id OR MEMBER_ID = :member2Id GROUP BY CHAT_ROOM_ID) AS FOO WHERE MEMBER_NUM=2", nativeQuery = true)
    Optional<Long> findOne2OneRoomNumber(@Param("member1Id") Long member1Id, @Param("member2Id") Long member2Id);
}
