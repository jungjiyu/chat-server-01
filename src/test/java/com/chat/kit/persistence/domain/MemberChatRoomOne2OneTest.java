package com.chat.kit.persistence.domain;

import com.chat.kit.persistence.repository.MemberChatRoomRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


@SpringBootTest
class MemberChatRoomTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private MemberChatRoomRepository repository;

    @Test
    @Transactional()
    public void findRoom(){
        //member1과 member2가 room No1로
        //member2와 member3가 room No2로 채팅
        Member member1 = new Member(1L);
        Member member2 = new Member(2L);
        Member member3 = new Member(3L);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);

        ChatRoom room1 = new ChatRoom();
        ChatRoom room2 = new ChatRoom();

        em.persist(room1);
        em.persist(room2);

        MemberChatRoom memberChatRoomOne2One1 = new MemberChatRoom();
        MemberChatRoom memberChatRoomOne2One2 = new MemberChatRoom();
        MemberChatRoom memberChatRoomOne2One3 = new MemberChatRoom();
        MemberChatRoom memberChatRoomOne2One4 = new MemberChatRoom();

        //member1과 member2가 room No1번으로 채팅
        memberChatRoomOne2One1.setMember(member1);
        memberChatRoomOne2One1.setChatRoom(room1);

        memberChatRoomOne2One2.setMember(member2);
        memberChatRoomOne2One2.setChatRoom(room1);

        //member2와 member3이 room No2번으로 채팅
        memberChatRoomOne2One3.setMember(member2);
        memberChatRoomOne2One3.setChatRoom(room2);

        memberChatRoomOne2One4.setMember(member3);
        memberChatRoomOne2One4.setChatRoom(room2);

        em.persist(memberChatRoomOne2One1);
        em.persist(memberChatRoomOne2One2);
        em.persist(memberChatRoomOne2One3);
        em.persist(memberChatRoomOne2One4);

//        List<MemberChatRoom> byMember = repository.findByMember(member2);
        //List<MemberChatRoom> byMember = repository.findByTwoMember(member1,member2);
        //byMember.forEach(v-> System.out.println("user: " + v.getMember().getName() +", room No:"+ v.getChatRoom().getId()));

        //List<Object[]> byTwoMemberGroup = repository.findByTwoMemberGroup(member1, member2);
        //byTwoMemberGroup.forEach(v-> System.out.println("v = " + v));
        Long roomNumber12 = repository.findOne2OneRoomNumber(member1.getId(),member2.getId());
        System.out.println("roomNumber = " + roomNumber12);

        //Long roomNumber23 = repository.findOne2OneRoomNumber(member2.getId(), member3.getId());
        //System.out.println("roomNumber = " + roomNumber23);


    }
}