package com.flc.memberbooking.repository;

import com.flc.memberbooking.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Integer> {
}
