package com.chat.kit.service;

import com.chat.kit.api.request.FindOne2OneChatRoomDto;
import com.chat.kit.api.request.RequestChatMessage;
import com.chat.kit.api.response.ApiResponse;
import com.chat.kit.api.response.common.ChatRoomListResponse;
import com.chat.kit.api.response.common.ChatRoomMessagesResponse;
import com.chat.kit.api.response.common.FindChatRoomResponse;
import com.chat.kit.api.response.common.error.ErrorCode;
import com.chat.kit.api.response.common.success.ResponseCode;
import com.chat.kit.customException.NoChatRoomException;
import com.chat.kit.customException.NoMemberException;
import com.chat.kit.persistence.domain.*;
import com.chat.kit.persistence.repository.ChatMessageRepository;
import com.chat.kit.persistence.repository.ChatRoomRepository;
import com.chat.kit.persistence.repository.MemberChatRoomRepository;
import com.chat.kit.persistence.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.id.IncrementGenerator;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class ChatService {
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberChatRoomRepository memberChatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AuthService authService;

    public ChatRoomListResponse getChatRoomId(FindOne2OneChatRoomDto request){
        if(request.getMemberIds().size()!=2){
            return null;
        }

        Optional<Long> one2OneRoomNumber = memberChatRoomRepository.findOne2OneRoomNumber(request.getMemberIds().get(0), request.getMemberIds().get(1));

        if(one2OneRoomNumber.isPresent()){
            Optional<ChatMessage> lastMsgBox = chatMessageRepository.findLastMsgByChatRoomId(one2OneRoomNumber.get());
            String lastMsg = "";

            if(lastMsgBox.isPresent()){
                lastMsg = lastMsgBox.get().getMessage();
            }

            return ChatRoomListResponse.of(one2OneRoomNumber.get(), request.getMemberIds(), MessageReadStatus.UN_REDD, lastMsg);
        }else{
            Long newRoomId = createNewRoom(request.getMemberIds().get(0), request.getMemberIds().get(1));
            return ChatRoomListResponse.of(newRoomId, request.getMemberIds(), MessageReadStatus.UN_REDD, "");
        }
    }

    private Long createNewRoom(Long member1Id, Long member2Id){
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
    public List<ChatMessage> findChatMessages(Long chatRoomId){
        Optional<ChatRoom> findChatRoom = chatRoomRepository.findById(chatRoomId);
        if(findChatRoom.isPresent()){
            return chatMessageRepository.findBychatRoom(findChatRoom.get());
        }else{
            throw new NoChatRoomException("The chat room id you requested does not exist");
        }
    }

    public List<ChatRoomListResponse> getChatRoomList(){
        Member loginMember = authService.getLoginMember();
        return getChatRoomList(loginMember.getId());
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

    public List<ChatRoomMessagesResponse> findUnreadChats() {
        Member loginMember = authService.getLoginMember();

        List<ChatMessage> messages = chatMessageRepository.findUnReadMsgByMemberId(loginMember.getId());
        return messages.stream()
                .map(ChatRoomMessagesResponse::of)
                .collect(Collectors.toList());
    }

}
