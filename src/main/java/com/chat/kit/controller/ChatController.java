package com.chat.kit.controller;

import com.chat.kit.api.request.FindChatRoomDto;
import com.chat.kit.api.request.RequestChatMessage;
import com.chat.kit.api.response.ApiResponse;
import com.chat.kit.api.response.common.ChatRoomListResponse;
import com.chat.kit.api.response.common.ChatRoomMessagesResponse;
import com.chat.kit.api.response.common.error.ErrorCode;
import com.chat.kit.api.response.common.success.ResponseCode;
import com.chat.kit.persistence.domain.ChatMessage;
import com.chat.kit.persistence.domain.Member;
import com.chat.kit.persistence.repository.ChatMessageRepository;
import com.chat.kit.persistence.repository.MemberChatRoomRepository;
import com.chat.kit.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//@RequestMapping("/chat")
@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final SimpMessageSendingOperations template;
    private List<Long> onChattingMembers = new ArrayList<>();
    private final MemberChatRoomRepository memberChatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpUserRegistry simpUserRegistry;


    //특정 회원의 채팅 목록 조회

    /**
     * 현재 로그인한 회원이 참여중인  (1:1 혹은 그룹 ) 채팅방 목록 반환
     */
    @GetMapping("/chat/chatRooms")
    public ApiResponse<List<ChatRoomListResponse>> getChatRoomList(@AuthenticationPrincipal Member loginMember) {
        return ApiResponse.response(ResponseCode.OK,  chatService.getChatRoomList(loginMember.getId()));
    }


    /**
     * n 개의 멤버를 받아 (1:1 혹은 그룹 )채팅방의 PK를 반환.
     * if : n 개의 멤버에 대해 이미 존재하는 채팅방이 없을 경우 새로 생성시킴
     */
    @PostMapping("/chat/room")
    public ApiResponse<ChatRoomListResponse> getChatRoomId(@RequestBody FindChatRoomDto request){
        ChatRoomListResponse chatRoomListResponse = chatService.getChatRoomId(request);

        if(chatRoomListResponse == null){
            return ApiResponse.response(ErrorCode.PK_NUMBER_NOT_ENOUGH, null);
        }else{
            return ApiResponse.response(ResponseCode.OK, chatRoomListResponse);
        }
    }

    /**
     * 채팅방의id 를 받아 (1:1 혹은 그룹 )채팅방의 메세지들을 반환.
     * */
    @GetMapping("/chat/{roomId}/messages")
    public ApiResponse<List<ChatRoomMessagesResponse>> getChatMessages(@PathVariable Long roomId){

        List<ChatMessage> chatMessages = chatService.findChatMessages(roomId);
        List<ChatRoomMessagesResponse> data = chatMessages.stream()
                .map(ChatRoomMessagesResponse::of)
                .collect(Collectors.toList());
        return ApiResponse.response(ResponseCode.OK, data);

    }

    /**
     * 현재 로그인한 멤버가 읽지 않은 메시지들을 반환
     * */
    @GetMapping("/chat/unread-chats")
    public ApiResponse<List<ChatRoomMessagesResponse>> findUnreadChats(@AuthenticationPrincipal Member loginMember){
        List<ChatRoomMessagesResponse> unreadChats = chatService.findUnreadChats(loginMember.getId());
        return ApiResponse.response(ResponseCode.OK, unreadChats);
    }

    //메시지 송신 및 수신, /pub가 생략된 모습. 클라이언트 단에선 /pub/message로 요청
    @MessageMapping("/message")
    @Transactional
    public void receiveMessage(@RequestBody RequestChatMessage chat) {
        log.info("{} 받음",chat);

        // 메시지를 저장
        ChatMessage chatMessage = chatService.saveMessage(chat);
        // 메시지를 해당 채팅방 구독자들에게 전송
        template.convertAndSend("/sub/chatroom/"+chat.getRoomId(), ChatRoomMessagesResponse.of(chatMessage));//현재 방에 들어와있는 사람
        log.info("메시지를 채팅방 {} 구독자들에게 전송 완료",chat.getRoomId());

        Set<Long> allChatMemberIds = memberChatRoomRepository.findByChatRoomId(chat.getRoomId()).stream()
                .filter(mcr -> !mcr.getMember().getId().equals(chat.getSenderId()))
                .map(mcr -> mcr.getMember().getId())
                .collect(Collectors.toSet());

        Set<Long> inChatRoomMemberIds = simpUserRegistry.getUsers().stream()
                .filter(
                        user -> user.getSessions().stream()
                                .anyMatch(
                                        session -> session.getSubscriptions().stream()
                                                .anyMatch(sub -> sub.getDestination().equals("/sub/chatroom/"+chat.getRoomId()))
                                )
                )
                .map(user -> Long.parseLong(user.getName()))
                .collect(Collectors.toSet());

        allChatMemberIds.removeAll(inChatRoomMemberIds);

        // 상단 알림용
        allChatMemberIds.forEach(memberId -> {
            template.convertAndSend("/sub/myRoom/"+memberId, ChatRoomMessagesResponse.of(chatMessage));
        });

    }
}
