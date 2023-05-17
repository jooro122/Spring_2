package com.project.easytrip.controller;

import com.project.easytrip.dto.MemberAdapter;
import com.project.easytrip.dto.MemberDTO;
import com.project.easytrip.entity.Member;
import com.project.easytrip.entity.SkyRerv;
import com.project.easytrip.repository.MemberRepository;
import com.project.easytrip.service.MemberService;
import com.project.easytrip.service.SkyRervService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

	@Autowired
    private final MemberService memberService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MemberRepository  memberRepository;

    @Autowired
    private SkyRervService skyRervService;


    @GetMapping("/loginPage")
    public String loginPage(){

        return "member/loginpage";
    }


//    @PostMapping("/login")
//    public String login(@RequestParam("memberId") String memberId,
//            @RequestParam("memberPassword") String memberPassword,MemberDTO memberDTO){
//    	memberDTO.setMemberPassword(passwordEncoder.encode(memberPassword));
//
//    	UserDetails userDetails = loginService.loadUserByUsername(memberId);
//
//        if (memberId != null) {
//            System.out.println(SecurityContextHolder.getContext().getAuthentication());
//            return "redirect:/";
//
//        }else {
//            // 비밀번호가 일치하지 않는 경우
//            return "redirect:/member/loginPage";
//        }
//    }
    @GetMapping("/signup")
    public String signUp(){
        return "member/signup";
    }

    @PostMapping("/signup")
    public String join(MemberDTO memberDTO){

        memberService.dtoToEntity(memberDTO);

        return "redirect:/member/loginPage";

    }

    // 아이디 중복검사
    @GetMapping("/idCheck")
    @ResponseBody
    public ResponseEntity<?> overlappedID(@RequestParam String id) {
        boolean isOverlapped = memberService.overlappedID(id);
        return ResponseEntity.ok(isOverlapped);
    }

    // 이메일 중복검사
    @GetMapping("/emailCheck")
    @ResponseBody
    public ResponseEntity<?> overlappedEmail(@RequestParam String email) {
        boolean isOverlapped = memberService.overlappedEmail(email);
        return ResponseEntity.ok(isOverlapped);
    }

    @GetMapping("/mypage")
    public String getMyPage(@AuthenticationPrincipal MemberAdapter memberAdapter, Model model) {

        Member member = memberAdapter.getMember();

        model.addAttribute("member",member);

        return "member/mypage";
    }

    @ResponseBody
    @PostMapping("/mypage")
    public String update(@ModelAttribute MemberDTO memberDTO) {

        String message = "";
        // 받아온 회원 정보를 업데이트 합니다.
        memberService.update(memberDTO);


        // 업데이트된 회원의 상세 정보 페이지로 리다이렉트 합니다.
        message = "<script>alert('회원 정보가 수정되었습니다 다시 로그인 하세요!');location.href='/member/logout';</script>";
        return message;
    }
    @GetMapping("/updatePassword")
    public String updatePassword(@AuthenticationPrincipal MemberAdapter memberAdapter, Model model) {
        // 현재 세션에 저장된 "loginId" 속성 값을 가져와 myId 변수에 저장합니다.
        Member member = memberAdapter.getMember();

        model.addAttribute("member", member);

        return "member/updatePassword";
    }

    @ResponseBody
    @PostMapping("/updatePassword")
    public String updatePasswordProcess(MemberDTO memberDTO, String newPassword) {
        String message = "";
        Optional<Member> byMemberId = memberRepository.findByMemberId(memberDTO.getMemberId());
        Member member = byMemberId.get();
        if (!passwordEncoder.matches(member.getMemberPassword(),memberDTO.getMemberPassword())) {
            message = "<script>alert('틀린 비밀번호 입니다.');location.href='/member/updatePassword';</script>";
            return message;
        }

        // 비밀번호 변경 로직을 수행하는 서비스 메서드를 호출합니다.
        memberService.updatePassword(memberDTO.getMemberId(), newPassword);

        // 변경이 완료되면 리다이렉트를 통해 원하는 경로로 이동합니다. (예: 메인 페이지)
        message = "<script>alert('비밀번호 변경이 완료되었습니다.');location.href='/member/mypage';</script>";
        return message;
    }
    @GetMapping("/reservation")
    private String getReservation(Model model,@AuthenticationPrincipal MemberAdapter memberAdapter) {
        Member member = memberAdapter.getMember();

        String name = memberAdapter.getMember().getMemberName();
        String phone = memberAdapter.getMember().getMemberPhoneNumber();

        System.out.println("check-=-=-=-" + name + " " + phone);

        List<SkyRerv> rerv = skyRervService.getPassenger(name, phone, model);

        model.addAttribute("member", member);
        model.addAttribute("reservationList", rerv);

        return "member/reservation";
    }

}
