package com.chat.kit.controller;

import com.chat.kit.api.request.FindOne2OneChatRoomDto;
import com.chat.kit.api.request.RequestChatMessage;
import com.chat.kit.api.response.ApiResponse;
import com.chat.kit.api.response.common.ChatRoomListResponse;
import com.chat.kit.api.response.common.ChatRoomMessagesResponse;
import com.chat.kit.api.response.common.FindChatRoomResponse;
import com.chat.kit.api.response.common.error.ErrorCode;
import com.chat.kit.api.response.common.success.ResponseCode;
import com.chat.kit.persistence.domain.ChatMessage;
import com.chat.kit.persistence.domain.Member;
import com.chat.kit.persistence.domain.MemberChatRoom;
import com.chat.kit.persistence.domain.MessageReadStatus;
import com.chat.kit.persistence.repository.ChatMessageRepository;
import com.chat.kit.persistence.repository.MemberChatRoomRepository;
import com.chat.kit.service.AuthService;
import com.chat.kit.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    @GetMapping("/chatRooms")
    public ApiResponse<List<ChatRoomListResponse>> retrieveChatRoomList(){
        List<ChatRoomListResponse> chatRoomList = chatService.getChatRoomList();
        return ApiResponse.response(ResponseCode.OK,chatRoomList);
    }

    //두 명의 PK를 받아 이전에 방을 생성한 이력이 있다면 기존 방 PK를
    //없다면 신규 방을 생성 후 PK 전달
    @PostMapping("/chat/room")
    public ApiResponse<ChatRoomListResponse> getChatRoomId(@RequestBody FindOne2OneChatRoomDto request){
        ChatRoomListResponse chatRoomListResponse = chatService.getChatRoomId(request);

        if(chatRoomListResponse == null){
            return ApiResponse.response(ErrorCode.PK_NUMBER_NOT_ENOUGH, null);
        }else{
            return ApiResponse.response(ResponseCode.OK, chatRoomListResponse);
        }
    }
    // 채팅방 이전 메시지 반환
    @GetMapping("/chat/{roomId}/messages")
    public ApiResponse<List<ChatRoomMessagesResponse>> getChatMessages(@PathVariable Long roomId){

        List<ChatMessage> chatMessages = chatService.findChatMessages(roomId);
        List<ChatRoomMessagesResponse> data = chatMessages.stream()
                .map(ChatRoomMessagesResponse::of)
                .collect(Collectors.toList());
        return ApiResponse.response(ResponseCode.OK, data);

    }

    @GetMapping("/unread-chats")
    public ApiResponse<List<ChatRoomMessagesResponse>> findUnreadChats(){
        List<ChatRoomMessagesResponse> unreadChats = chatService.findUnreadChats();
        return ApiResponse.response(ResponseCode.OK, unreadChats);
    }

    //메시지 송신 및 수신, /pub가 생략된 모습. 클라이언트 단에선 /pub/message로 요청
    @MessageMapping("/message")
    @Transactional
    public void receiveMessage(@RequestBody RequestChatMessage chat) {

        // 메시지를 저장
        ChatMessage chatMessage = chatService.saveMessage(chat);
        // 메시지를 해당 채팅방 구독자들에게 전송
        template.convertAndSend("/sub/chatroom/"+chat.getRoomId(), ChatRoomMessagesResponse.of(chatMessage));//현재 방에 들어와있는 사람

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
