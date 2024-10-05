package com.chat.kit.service;

import com.chat.kit.api.request.MemberDto;
import com.chat.kit.persistence.domain.Member;
import com.chat.kit.persistence.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Transactional
@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public void createMember(@RequestBody MemberDto memberDto) {
        checkSystemAuthorization();

        if(memberDto.getId()==null){
            throw new RuntimeException("Member id is null");
        }

        if(memberRepository.findById(memberDto.getId()).isPresent()) {
            throw new RuntimeException("Member already exists");
        }

        memberRepository.save(
                Member.builder().id(memberDto.getId()).build()
        );
    }

    private void checkSystemAuthorization(){
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        if(!jwt.getClaim("role").equals("SYSTEM")){
            throw new RuntimeException("You are not authorized to perform this operation");
        }
    }

}
