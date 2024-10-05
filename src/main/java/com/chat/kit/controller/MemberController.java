package com.chat.kit.controller;

import com.chat.kit.api.request.MemberDto;
import com.chat.kit.persistence.domain.Member;
import com.chat.kit.persistence.repository.MemberRepository;
import com.chat.kit.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping
    public String createMember(@RequestBody MemberDto memberDto) {
        memberService.createMember(memberDto);
        return "created";
    }
}
