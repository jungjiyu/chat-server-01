package com.chat.kit.service;

import com.chat.kit.persistence.domain.*;
import com.chat.kit.persistence.repository.ChatMessageRepository;
import com.chat.kit.persistence.repository.ChatRoomRepository;
import com.chat.kit.persistence.repository.MemberChatRoomRepository;
import com.chat.kit.persistence.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitData {
    private final InitService initService;
    @EventListener(ApplicationReadyEvent.class)
    public void initData() throws IOException {
        log.info("데이터 초기화 시작");
//        initService.init();
    }
    @Component
    @RequiredArgsConstructor
    @Transactional
    public static class InitService {
        private final MemberRepository memberRepository;
        private final ChatRoomRepository chatRoomRepository;
        private final MemberChatRoomRepository memberChatRoomRepository;
        private final ChatMessageRepository chatMessageRepository;
        public void init() throws IOException {
            initMember();
            initChatRoom();
            initMemberChatRoom();
            initChatMessage();
        }
        private void initMember(){
            initMember(1L);
            initMember(2L);
            initMember(3L);
        }

        private void initMember(Long memberId){
            Member member = Member.builder()
                    .id(memberId)
                    .build();
            memberRepository.save(member);
        }

        private void initChatRoom(){
            initChatRoom(RoomType.ONE2ONE);
            initChatRoom(RoomType.ONE2ONE);
        }
        private void initChatRoom(RoomType roomType){
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setRoomType(roomType);
            chatRoomRepository.save(chatRoom);
        }

        private void initMemberChatRoom(){
            //회원 번호 1번과 2번이 room 번호 1번에서 채팅했음
            //회원 번호 2번과 3번이 room 번호 2번에서 채팅했음
            initMemberChatRoom(1L,1L);
            initMemberChatRoom(2L,1L);

            initMemberChatRoom(2L,2L);
            initMemberChatRoom(3L,2L);
        }
        private void initMemberChatRoom(Long memberId, Long chatRoomId){
            Member findMember = memberRepository.findById(memberId).get();
            ChatRoom findChatRoom = chatRoomRepository.findById(chatRoomId).get();

            MemberChatRoom memberChatRoom = MemberChatRoom.builder()
                    .member(findMember)
                    .chatRoom(findChatRoom)
                    .lastLeavedTime(null)
                    .build();
            memberChatRoomRepository.save(memberChatRoom);
        }

        private void initChatMessage(){
            initChatMessage(1L,1L,"hello", ChatType.TEXT, LocalDateTime.now());
            initChatMessage(1L,1L,"are you in online?", ChatType.TEXT, LocalDateTime.now());
            initChatMessage(2L,1L,"what's up?", ChatType.TEXT, LocalDateTime.now());

            initChatMessage(2L,2L,"this is room for 2 and 3 ", ChatType.TEXT, LocalDateTime.now());
            initChatMessage(3L,2L,"thank you for invitation", ChatType.TEXT, LocalDateTime.now());
            initChatMessage(2L,2L,"your welcome", ChatType.TEXT, LocalDateTime.now());
        }
        private void initChatMessage(Long memberId, Long chatRoomId, String message, ChatType chatType, LocalDateTime sentAt){

            ChatRoom findChatRoom = chatRoomRepository.findById(chatRoomId).get();

            ChatMessage chatMessage = ChatMessage.builder()
                    .memberId(memberId)
                    .chatRoom(findChatRoom)
                    .message(message)
                    .chatType(chatType)
                    .sentAt(sentAt)
                    .build();
            System.out.println("chatMessage.getId() = " + chatMessage.getId());
            chatMessageRepository.save(chatMessage);
        }
    }

}
