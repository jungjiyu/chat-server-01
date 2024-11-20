package com.chat.kit.service;

import com.chat.kit.api.request.FindChatRoomDto;
import com.chat.kit.api.request.RequestChatMessage;
import com.chat.kit.api.response.common.ChatRoomListResponse;
import com.chat.kit.api.response.common.ChatRoomMessagesResponse;
import com.chat.kit.customException.NoChatRoomException;
import com.chat.kit.customException.NoMemberException;
import com.chat.kit.persistence.domain.*;
import com.chat.kit.persistence.repository.ChatMessageRepository;
import com.chat.kit.persistence.repository.ChatRoomRepository;
import com.chat.kit.persistence.repository.MemberChatRoomRepository;
import com.chat.kit.persistence.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * 1:1 , 1:n 채팅 구분 :
 *  1) chatroom get & create하는 로직
 *
 * 1:1 , 1:n 채팅 미구분 :
 *  1) chatroom의 message들 얻는 로직
 *  2) member의 읽지 않은 message들을 얻는 로직
 *  3) message를 생성 및 저장하는 로직
 *  4) 현재 사용자가 참여중인 채팅방 목록 반환
 *
 */
@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class ChatService {
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberChatRoomRepository memberChatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;



    public ChatRoomListResponse getChatRoomId(FindChatRoomDto request){
        return (request.getMemberIds().size()==2) ? getOne2OneChatRoomId( request) : getMultipleChatRoomId( request) ;
    }



    public ChatRoomListResponse getOne2OneChatRoomId(FindChatRoomDto request){
        log.info("getOne2OneChatRoomId 들어옴");
        // 일단 해당 멤버들이 속한 채팅방 조회한다
        Optional<Long> one2OneRoomNumber = memberChatRoomRepository.findOne2OneRoomNumber(request.getMemberIds().get(0), request.getMemberIds().get(1));

        if(one2OneRoomNumber.isPresent()){ // 만약 해당 멤버들이 속한 채팅방이 이미 존재하면 db 정보 가져온다
            log.info("해당 멤버들에 대한 채팅방 정보 이미 존재: {}", one2OneRoomNumber);
            String lastMsg = findLastMessageByRoomId(one2OneRoomNumber.get());
            return ChatRoomListResponse.of(one2OneRoomNumber.get(), request.getMemberIds(), MessageReadStatus.UN_REDD, lastMsg);

        }else{ // 만약 해당 멤버들이 속한 채팅방이 존재하지 않으면 새로 생성
            log.info("해당 멤버들에 대해 채팅방 새로 생성");
            Long newRoomId = createNewOne2OneChatRoom(request.getMemberIds().get(0), request.getMemberIds().get(1));
            return ChatRoomListResponse.of(newRoomId, request.getMemberIds(), MessageReadStatus.UN_REDD, "");
        }
    }


    public ChatRoomListResponse getMultipleChatRoomId(FindChatRoomDto request){
        log.info("getMultipleChatRoomId 들어옴");

        Optional<Long> multipleRoomNumber = memberChatRoomRepository.findMultipleRoomNumber(request.getMemberIds(), request.getMemberIds().size());

        if(multipleRoomNumber.isPresent()){
            log.info("해당 멤버들에 대한 채팅방 정보 이미 존재: {}",multipleRoomNumber );
            String lastMsg = findLastMessageByRoomId(multipleRoomNumber.get());
            return ChatRoomListResponse.of(multipleRoomNumber.get(), request.getMemberIds(), MessageReadStatus.UN_REDD, lastMsg);
        }else{
            log.info("해당 멤버들에 대해 채팅방 새로 생성");
            Long newRoomId = createNewMultipleChatRoom(request.getMemberIds());
            return ChatRoomListResponse.of(newRoomId, request.getMemberIds(), MessageReadStatus.UN_REDD, "");
        }
    }

    // TODO : 따로 채팅방 생성 로직만 구현 가능하게 하고 싶음 컨트롤러 단에 추가하세용
    public Long createNewChatRoom(List<Long> memberIds){
        Long  response =
                (memberIds.size()==2) ? createNewOne2OneChatRoom(memberIds.get(0), memberIds.get(1)) : createNewMultipleChatRoom( memberIds ) ;
        return response;
    }



    private Long createNewOne2OneChatRoom(Long member1Id, Long member2Id){
        Member member1 = memberRepository.findById(member1Id)
                .orElseThrow();
        Member member2 = memberRepository.findById(member2Id)
                .orElseThrow();

        ChatRoom createdChatRoom = ChatRoom.builder()
                .roomType(RoomType.ONE2ONE)
                .build();
        chatRoomRepository.save(createdChatRoom);

        MemberChatRoom memberChatRoom1 = MemberChatRoom.builder()
                .chatRoom(createdChatRoom)
                .member(member1)
                .lastLeavedTime(LocalDateTime.now())
                .build();
        MemberChatRoom memberChatRoom2 = MemberChatRoom.builder()
                .chatRoom(createdChatRoom)
                .member(member2)
                .lastLeavedTime(LocalDateTime.now())
                .build();

        memberChatRoomRepository.save(memberChatRoom1);
        memberChatRoomRepository.save(memberChatRoom2);

        return createdChatRoom.getId();
    }


    private Long createNewMultipleChatRoom(List<Long> memberIds){

        ChatRoom createdChatRoom = ChatRoom.builder()
                .roomType(RoomType.MULTIPLE)
                .build();
        chatRoomRepository.save(createdChatRoom);


        memberIds.forEach(memberId -> {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new NoMemberException("Member not found"));
            MemberChatRoom memberChatRoom = MemberChatRoom.builder()
                    .chatRoom(createdChatRoom)
                    .member(member)
                    .lastLeavedTime(LocalDateTime.now())
                    .build();
            memberChatRoomRepository.save(memberChatRoom);
        });


        return createdChatRoom.getId();
    }


    public List<ChatMessage> findChatMessages(Long chatRoomId){
        Optional<ChatRoom> findChatRoom = chatRoomRepository.findById(chatRoomId);
        if(findChatRoom.isPresent()){
            return chatMessageRepository.findBychatRoom(findChatRoom.get());
        }else{
            throw new NoChatRoomException("The chat room id you requested does not exist");
        }
    }

    public List<ChatRoomListResponse> getChatRoomList(Long memberId){
        Optional<Member> findMember = memberRepository.findById(memberId);
        if(findMember.isPresent()){//존재하는 회원이라면
            List<ChatRoomListResponse> res = new ArrayList<>();
            List<MemberChatRoom> memberChatRoom = findMember.get().getMemberChatRoom();//회원이 참여한 채팅방 조회
            for(MemberChatRoom mc :memberChatRoom){//방별로 방ID, 방에 참여한 회원 id, 안읽은 메시지 상태를 응답으로 생성
                ChatRoom chatRoom = mc.getChatRoom();
                List<Long> memberIds = chatRoom.getMemberChatRoomOne().stream().map(r->r.getMember().getId()).toList();//특정 방에 참여한 회원정보 조회

                String lastMsg = "";
                Optional<ChatMessage> lastMsgBox = chatMessageRepository.findLastMsgByChatRoomId(chatRoom.getId());
                if(lastMsgBox.isPresent()){
                    lastMsg = lastMsgBox.get().getMessage();
                }

                ChatRoomListResponse chatRoomListResponse = ChatRoomListResponse.of(chatRoom.getId(), memberIds, MessageReadStatus.ALL_READ, lastMsg);
                res.add(chatRoomListResponse);
            }
            return res;
        }else{
            throw new NoMemberException("The member you requested does not exist");
        }
    }
    public ChatMessage saveMessage(RequestChatMessage requestChatMessage){
        Optional<ChatRoom> findChatRoom = chatRoomRepository.findById(requestChatMessage.getRoomId());
        Optional<Member> findMember = memberRepository.findById(requestChatMessage.getSenderId());
        if(findChatRoom.isPresent()&&findMember.isPresent()){
            ChatMessage chatMessage = ChatMessage.builder()
                    .chatRoom(findChatRoom.get())
                    .memberId(requestChatMessage.getSenderId())
                    .message(requestChatMessage.getMessage())
                    .chatType(requestChatMessage.getChatType())
                    .sentAt(LocalDateTime.now())
                    .build();
            chatMessageRepository.save(chatMessage);

            return chatMessage;
        }else{
             throw new RuntimeException("the member or room does not exist");
        }
    }



    public List<ChatRoomMessagesResponse> findUnreadChats(Long memberId) {
        List<ChatMessage> messages = chatMessageRepository.findUnReadMsgByMemberId(memberId);
        return messages.stream()
                .map(ChatRoomMessagesResponse::of)
                .collect(Collectors.toList());
    }

    private String findLastMessageByRoomId(Long roomId) {
        return chatMessageRepository.findLastMsgByChatRoomId(roomId)
                .map(ChatMessage::getMessage)
                .orElse("");
    }



}
