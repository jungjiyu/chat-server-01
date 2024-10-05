package com.chat.kit.persistence.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Member {
    @Id
    @Column(name = "MEMBER_ID")
    private Long id;
    public Member() {
    }

    @Builder
    public Member(Long id) {
        this.id = id;
    }
    @OneToMany(mappedBy = "member")
    @Builder.Default
    private List<MemberChatRoom> memberChatRoom = new ArrayList<>();
}
