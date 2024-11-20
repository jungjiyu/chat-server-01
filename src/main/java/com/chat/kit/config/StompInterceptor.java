package com.chat.kit.config;

import com.chat.kit.persistence.domain.MemberChatRoom;
import com.chat.kit.persistence.repository.MemberChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class StompInterceptor implements ChannelInterceptor {
    private final JwtDecoder jwtDecoder;
    private final MemberChatRoomRepository memberChatRoomRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            log.info("preSend method called with command: {}", accessor.getCommand());

            // CONNECT 처리 부분
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                log.info("CONNECT 부분 해석 시작");

                String memberIdHeader = accessor.getFirstNativeHeader("memberId");
                log.info("memberIdHeader : {} ", memberIdHeader);

                if (memberIdHeader != null) {
                    Long memberId = Long.parseLong(memberIdHeader);
                    // JWT 사용안하는데 JwtAuthenticationToken 썼더니 에러났다. UsernamePasswordAuthenticationToken 을 쓰니 해결됬다.
                    Authentication user = new UsernamePasswordAuthenticationToken(memberId.toString(), null, null); //
                    accessor.setUser(user); // 세션에 사용자 정보 설정
                    log.info("{} 멤버 소켓 연결", memberId);
                } else {
                    log.info("CONNECT 메시지에 memberId가 누락되었습니다.");
                    throw new RuntimeException("memberId 헤더가 없습니다.");
                }
            }

            // DISCONNECT 처리 부분
            else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                log.info("DISCONNECT 메시지 처리 시작");

                if (accessor.getUser() != null) {
                    Long memberId = Long.parseLong(accessor.getUser().getName());
                    log.info("memberId {}의 DISCONNECT 처리 시작", memberId);

                    // 추가적으로 어떤 이유로 DISCONNECT가 호출되는지 원인을 파악
                    if (memberId != null) {
                        log.info("사용자 {}의 마지막 연결 시간 업데이트 중...", memberId);
                        // 실제로 DB 업데이트 로직이 필요하다면 여기에 작성
                    } else {
                        log.info("DISCONNECT 메시지에 사용자 정보가 없습니다.");
                    }
                } else {
                    log.info("DISCONNECT 메시지에서 사용자 정보를 찾을 수 없습니다.");
                }
            }
        } else {
            log.info("StompHeaderAccessor is null in preSend method");
        }

        return message;
    }

}
