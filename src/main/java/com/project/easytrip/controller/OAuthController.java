package com.project.easytrip.controller;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.project.easytrip.entity.Member;
import com.project.easytrip.service.LoginService;
import com.project.easytrip.service.OAuthService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/oauth")
public class OAuthController {

	private final OAuthService oAuthService;
	
	@GetMapping("/kakao")
	public void redirectToKakaoLogin(HttpServletRequest request, HttpServletResponse response,
	                                 @RequestParam(required = false) String code) throws IOException {
	    if (code != null) {
	    	Member member = oAuthService.signupWithKakao(code);
	        if (member != null) {
                // 회원가입 성공 시 자동으로 로그인 처리
	        	UserDetails userDetails = oAuthService.loadUserByUsername(member.getMemberId());
                // 로그인 후 리디렉션할 URL 설정
                response.sendRedirect("/"); // 원하는 URL로 대체
            } else {
                // 회원가입 실패 시 처리할 내용
                response.sendRedirect("/member/loginPage"); // 회원가입 실패 페이지 또는 로그인 페이지로 대체
            }
	    } else {
	        response.sendRedirect("https://kauth.kakao.com/oauth/authorize?client_id=ec14b2fb34c51946d4d05d3d73912313&redirect_uri=http://localhost:8080/oauth/kakao&response_type=code");
	    }
	}
	
}
