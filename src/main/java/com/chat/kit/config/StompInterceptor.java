package com.chat.kit.config;

import com.chat.kit.persistence.domain.MemberChatRoom;
import com.chat.kit.persistence.repository.MemberChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Transactional
@RequiredArgsConstructor
public class StompInterceptor implements ChannelInterceptor {
    private final JwtDecoder jwtDecoder;
    private final MemberChatRoomRepository memberChatRoomRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                Jwt jwt = jwtDecoder.decode(token);
                Long memberId = (Long)jwt.getClaims().get("memberId");
                Authentication user = new JwtAuthenticationToken(jwt, null, memberId.toString());
                accessor.setUser(user);
            }else{
                throw new RuntimeException();
            }
        }

        else if(StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())){
            if(!accessor.getNativeHeader("dest").isEmpty() && accessor.getNativeHeader("dest").get(0).contains("/sub/chatroom/")){
                String destination = accessor.getNativeHeader("dest").get(0);
                Long chatRoomId = Long.parseLong(destination.substring(destination.lastIndexOf("/")+1));
                Long memberId = Long.parseLong(accessor.getUser().getName());
                MemberChatRoom memberChatRoom = memberChatRoomRepository.findByChatRoomIdAndMemberId(chatRoomId, memberId)
                        .orElseThrow();

                memberChatRoom.setLastLeavedTime(LocalDateTime.now());
            }
        }
        //TODO : DISCONNECT일때도 leavedTime update??

        return message;
    }
}
