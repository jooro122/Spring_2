package com.project.easytrip.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.easytrip.entity.Member;


public interface MemberRepository extends JpaRepository<Member, String> {
	
	Optional<Member> findByMemberId(String memberId);
	Optional<Member> findByMemberEmail(String memberEmail);


}
