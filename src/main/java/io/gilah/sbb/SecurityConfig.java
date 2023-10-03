package io.gilah.sbb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration		// 스프링의 환경설정 파일임을 의미하는 애너테이션. 스프링 시큐리티의 설정을 위해 사용되었음
@EnableWebSecurity  //  모든 요청 URL이 스프링 시큐리티의 제어를 받도록 만드는 애너테이션. @EnableWebSecurity 애너테이션을 사용하면 내부적으로 SpringSecurityFilterChain이 동작하여 URL 필터가 적용
@EnableMethodSecurity(prePostEnabled = true) // AnswerController, QuestionController에서 사용한 @PreAuthorize 애너테이션을 사용하기 위해 꼭 필요
public class SecurityConfig {
	@Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(
            		(authorizeHttpRequests) -> authorizeHttpRequests
            		.requestMatchers(new AntPathRequestMatcher("/**")).permitAll()) //스프링 시큐리티의 세부 설정은 SecurityFilterChain 빈을 생성하여 설정할 수 있다. 다음 문장은 모든 인증되지 않은 요청을 허락한다는 의미이다. 따라서 로그인을 하지 않더라도 모든 페이지에 접근할 수 있다.
            .csrf((csrf) -> csrf
                    .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))) // H2 콘솔의 화면이 frame 구조로 작성되었기 때문에 첫화면은 보여도 DB에 접속하면 깨지게된다. 스프링 시큐리티는 사이트의 콘텐츠가 다른 사이트에 포함되지 않도록 하기 위해 X-Frame-Options 헤더값을 사용하여 이를 방지한다. (clickjacking 공격을 막기위해 사용함)
            .headers((headers) -> headers
                    .addHeaderWriter(new XFrameOptionsHeaderWriter(
                        XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))) // URL 요청시 X-Frame-Options 헤더값을 sameorigin으로 설정하여 오류가 발생하지 않도록 했다. X-Frame-Options 헤더의 값으로 sameorigin을 설정하면 frame에 포함된 페이지가 페이지를 제공하는 사이트와 동일한 경우에는 계속 사용할 수 있다.
            .formLogin((formLogin) -> formLogin
                    .loginPage("/user/login")
                    .defaultSuccessUrl("/"))
            .logout((logout) -> logout
                    .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true));  // 생성된 사용자 세션 삭제
           return http.build();
    }
	// 비밀번호 암호화
	@Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
	// AuthenticationManager: Security 인증을 담당하는 클래스
	// 이 클래스는 사용자 인증 시 UserSecurityService와 PasswordEncoder를 사용한다.
	@Bean
	AuthenticationManager authenticationmanager(AuthenticationConfiguration authenticationConfigration) throws Exception{
		return authenticationConfigration.getAuthenticationManager();
	}
}
