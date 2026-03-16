package com.team4.moin.common.configs;

import com.team4.moin.common.auth.JwtTokenFilter;
import com.team4.moin.common.exception.JwtAuthenticationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;


import com.team4.moin.common.auth.JwtTokenFilter;
import com.team4.moin.common.exception.JwtAuthenticationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;
    private final JwtAuthenticationHandler jwtAuthenticationHandler;
    @Autowired
    public SecurityConfig(JwtTokenFilter jwtTokenFilter, JwtAuthenticationHandler jwtAuthenticationHandler) {
        this.jwtTokenFilter = jwtTokenFilter;
        this.jwtAuthenticationHandler = jwtAuthenticationHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(c->c.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(a->a.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e->e
                        .authenticationEntryPoint(jwtAuthenticationHandler))
                .authorizeHttpRequests(a->a.requestMatchers("/user/create",
                                "/user/doLogin",
                                "/user/google/doLogin",
                                "/user/kakao/doLogin",
                                "/user/info-update",
                                "/user/refresh-at",
                                "/user/send-code",
                                "/user/send-code-signup",
                                "/user/verify-code",
                                "/user/password-update",
//                                채팅 연결 추가
                                "/connect/**",
                                "user/presigned-url",
                                "/error"
                        )
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/crew/my-*").authenticated()
                        // 비로그인도 크루 목록/상세 조회 가능
                        .requestMatchers(HttpMethod.GET, "/crew/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/meeting/crew/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/crewMember/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/feed/crew/**").permitAll()
                        .anyRequest()
                        .authenticated())
                .build();
    }

    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://www.moiin.shop"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder pwEncoder(){

        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}

