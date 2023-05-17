package com.project.easytrip.config;

import com.project.easytrip.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final LoginService loginService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedDoubleSlash(true);
        web.httpFirewall(firewall);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(loginService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {


        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/member/mypage").hasRole("MEMBER")
                .antMatchers("/member/updatePassword").hasRole("MEMBER")
                .antMatchers("/cars2").hasRole("MEMBER")
                .regexMatchers("^.*(?<!/)/{2}.*$").permitAll()


                .and().formLogin()
                .loginPage("/member/loginPage")
                .defaultSuccessUrl("/")
                .failureUrl("/member/loginPage")
                .usernameParameter("memberId")
                .passwordParameter("memberPassword")
                .loginProcessingUrl("/member/login")
                .successHandler(new AuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        System.out.println("authentication : " + authentication.getName());
                        response.sendRedirect("/");
                    }
                })
                .failureHandler(new AuthenticationFailureHandler() {
                    @Override
                    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                        System.out.println("exception : " + exception.getMessage());
                        response.sendRedirect("/member/loginPage");
                    }
                })
                .permitAll()


                .and().logout()
                .logoutUrl("/member/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutRequestMatcher(new AntPathRequestMatcher("/member/logout"))
                .logoutSuccessHandler((request, response, authentication) -> {
                    // 카카오 로그아웃
                    String redirectUrl = "https://kauth.kakao.com/oauth/logout" +
                            "?client_id=ec14b2fb34c51946d4d05d3d73912313" +
                            "&logout_redirect_uri=http://localhost:8080/";
                    response.sendRedirect(redirectUrl);
                })



//                .and()
//                .oauth2Login()
//                .loginPage("/oauth/kakao")


                .and().
                headers().frameOptions().sameOrigin()

                .and().requiresChannel()
                .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
                .requiresSecure();

//                .and()
//                .sessionManagement()
//                .maximumSessions(1) //세션 최대 허용 수
//                .maxSessionsPreventsLogin(false);




    }
}