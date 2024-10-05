package com.chat.kit.persistence.repository;

import com.chat.kit.persistence.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
