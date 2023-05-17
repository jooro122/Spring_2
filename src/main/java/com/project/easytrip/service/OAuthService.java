package com.project.easytrip.service;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.google.gson.JsonParser;
import com.project.easytrip.dto.MemberAdapter;
import com.project.easytrip.entity.Member;
import com.project.easytrip.entity.Role;
import com.project.easytrip.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@RequiredArgsConstructor
@Service
public class OAuthService implements UserDetailsService{
	
	private final MemberRepository memberRepository;
	private final LoginService loginService;

	
	public Member signupWithKakao(String code) {
	    String accessToken = "";
	    String reqURL = "https://kauth.kakao.com/oauth/token";

	    try {
	        URL url = new URL(reqURL);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("POST");
	        conn.setDoOutput(true);

	        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
	        StringBuilder sb = new StringBuilder();
	        sb.append("grant_type=authorization_code");
	        sb.append("&client_id=ec14b2fb34c51946d4d05d3d73912313"); // TODO REST_API_KEY 입력
	        sb.append("&redirect_uri=http://localhost:8080/oauth/kakao"); // TODO 인가코드 받은 redirect_uri 입력
	        sb.append("&code=" + code);
	        bw.write(sb.toString());
	        bw.flush();

	        int responseCode = conn.getResponseCode();
	        System.out.println("responseCode : " + responseCode);

	        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        String line = "";
	        String result = "";

	        while ((line = br.readLine()) != null) {
	            result += line;
	        }
	        System.out.println("response body : " + result);

	        JsonParser parser = new JsonParser();
	        JsonElement element = parser.parse(result);

	        accessToken = element.getAsJsonObject().get("access_token").getAsString();

	        br.close();
	        bw.close();
	        conn.disconnect();

	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    // 액세스 토큰을 사용하여 사용자 정보를 요청
	    if (!accessToken.isEmpty()) {
	        try {
	            URL userInfoUrl = new URL("https://kapi.kakao.com/v2/user/me");
	            HttpURLConnection userInfoConn = (HttpURLConnection) userInfoUrl.openConnection();
	            userInfoConn.setRequestMethod("GET");
	            userInfoConn.setRequestProperty("Authorization", "Bearer " + accessToken);

	            int userInfoResponseCode = userInfoConn.getResponseCode();
	            System.out.println("User Info Response Code : " + userInfoResponseCode);

	            BufferedReader userInfoBr = new BufferedReader(new InputStreamReader(userInfoConn.getInputStream()));
	            String userInfoLine;
	            StringBuilder userInfoResult = new StringBuilder();

	            while ((userInfoLine = userInfoBr.readLine()) != null) {
	                userInfoResult.append(userInfoLine);
	            }

	            userInfoBr.close();
	            userInfoConn.disconnect();

	            System.out.println("User Info Response Body : " + userInfoResult.toString());

	            JsonParser userInfoParser = new JsonParser();
	            JsonObject userInfoObject = userInfoParser.parse(userInfoResult.toString()).getAsJsonObject();

	            String kakaoUserId = userInfoObject.get("id").getAsString();
	            String nickname = userInfoObject.get("properties").getAsJsonObject().get("nickname").getAsString();
//	            String email = userInfoObject.get("kakao_account").getAsJsonObject().get("email").getAsString();
	            // DB에 저장할 회원 정보 생성
	            Member member = Member.builder()
	                    .memberId(kakaoUserId)
	                    .memberName(nickname)
//	                    .memberEmail(email)
	                    .memberPassword("")  // 카카오톡 소셜 로그인은 비밀번호가 필요하지 않으므로 임의의 값을 설정
	                    .memberPhoneNumber("")    // 필요에 따라 추가 정보 설정 가능
	                    .memberAddress("")
	                    .memberDetailAddress("")
	                    .role(Role.MEMBER.getValue())
	                    .build();

	            memberRepository.save(member);

	            if (member != null) {
	                // 회원 가입 성공
	                System.out.println("카카오톡 소셜 로그인 회원가입 성공");
	                UserDetails userDetails = loadUserByUsername(member.getMemberId());
	                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	                SecurityContextHolder.getContext().setAuthentication(authentication);
	                return member;
	            } else {
	                // 회원 가입 실패
	                System.out.println("카카오톡 소셜 로그인 회원가입 실패");
	                return null;
	            }

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    return null;
	}


	@Override
	public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {
		Member member = memberRepository.findByMemberId(memberId).orElseThrow(()
                -> new UsernameNotFoundException("회원 정보가 없습니다."));
		
		return new MemberAdapter(member);
	}
    
    
    
}