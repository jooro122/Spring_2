package com.project.easytrip.service;

import com.project.easytrip.dto.MemberDTO;
import com.project.easytrip.entity.Member;
import com.project.easytrip.entity.Role;
import com.project.easytrip.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MemberService{

    @Autowired
    private PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;



    public Member dtoToEntity(MemberDTO memberDTO){
        //DB에 아이디와 이메일이 있는지 확인후 있다면 Null 없다면 Member.builder() 로 넘어가 회원 정보 생성
        Optional<Member> byMemberId = memberRepository.findByMemberId(memberDTO.getMemberId());
        Optional<Member> byMemberEmail = memberRepository.findByMemberEmail(memberDTO.getMemberEmail());
        if(byMemberId.isPresent()){
            return null;
        }else if(byMemberEmail.isPresent()) {
            return null;
        }


        Member member = Member.builder()
                .memberId(memberDTO.getMemberId())
                .memberPassword(passwordEncoder.encode(memberDTO.getMemberPassword()))
                .memberName(memberDTO.getMemberName())
                .memberEmail(memberDTO.getMemberEmail())
                .memberPhoneNumber(memberDTO.getMemberPhoneNumber())
                .memberAddress(memberDTO.getMemberAddress())
                .memberDetailAddress(memberDTO.getMemberDetailAddress())
                .role(Role.MEMBER.getValue())
                .build();

        memberRepository.save(member);

        return member;
    }

    public boolean overlappedID(String memberId) {
        Optional<Member> memberOptional = memberRepository.findByMemberId(memberId);
        return memberOptional.isPresent();
    }

    // Email중복검사
    public boolean overlappedEmail(String memberEmail) {
        Optional<Member> memberOptional = memberRepository.findByMemberEmail(memberEmail);
        return memberOptional.isPresent();
    }

    @Transactional
    public void update(MemberDTO memberDTO){

        Member member = memberRepository.findByMemberId(memberDTO.getMemberId()).orElseThrow(()
        -> new UsernameNotFoundException("수정할 회원 정보가 없습니다."));

        member.setMemberName(memberDTO.getMemberName());
        member.setMemberPhoneNumber(memberDTO.getMemberPhoneNumber());

        memberRepository.save(member);
    }

    @Transactional
    public void updatePassword(String memberId, String newPassword) {
        // memberId에 해당하는 회원을 찾아서 비밀번호를 변경합니다.

        Optional<Member> optionalMemberEntity = memberRepository.findByMemberId(memberId);

        if (optionalMemberEntity.isPresent()) {
            Member member = optionalMemberEntity.get();
            member.setMemberPassword(passwordEncoder.encode(newPassword));

            // 변경된 비밀번호를 저장합니다.
            memberRepository.save(member);
        } else {
            // memberId에 해당하는 회원이 없을 경우 에러 처리를 수행합니다.
            // 이 부분은 필요에 따라 적절한 에러 처리를 구현하세요.
            throw new RuntimeException("회원 정보를 찾을 수 없습니다.");
        }
    }

}
