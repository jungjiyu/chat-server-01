package com.chat.kit.service;

import com.chat.kit.persistence.domain.Member;
import com.chat.kit.persistence.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final MemberRepository memberRepository;

    public Member getLoginMember(){
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long memberId = jwt.getClaim("memberId");

        Member member = memberRepository.findById(memberId)
                .orElseThrow();

        return member;
    }
}
