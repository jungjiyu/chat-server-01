package com.chat.kit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsUtils;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/**").permitAll()
                        .requestMatchers("/wss/**").permitAll()
//                        .anyRequest().authenticated()
                                .requestMatchers("/h2-console/**").permitAll()
                                .requestMatchers("/members/**").permitAll()
                                .requestMatchers("/chat/**").permitAll()
                                .anyRequest().permitAll() // 모든 요청을 허용

                )
                .cors().disable()
                .csrf().disable() // CSRF 비활성화 (Postman을 통한 테스트 시 편의성 제공)
                .headers(headers -> headers.frameOptions().sameOrigin()) // h2-consoel
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()));
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt()); // OAuth2 JWT 인증 설정

        return http.build();
    }



}
