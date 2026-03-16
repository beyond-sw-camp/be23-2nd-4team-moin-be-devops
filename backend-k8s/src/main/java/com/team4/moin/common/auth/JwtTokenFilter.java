package com.team4.moin.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtTokenFilter extends GenericFilter {
    @Value("${jwt.secretKey}")
    private String st_secret_ket;
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        try {


            HttpServletRequest req = (HttpServletRequest) request;
            String bearerToken = req.getHeader("Authorization");


            if (bearerToken == null) {
                chain.doFilter(request, response);
                return;
            }
            String token = bearerToken.substring(7);


            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(st_secret_ket)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();


            List<GrantedAuthority> authorityList = new ArrayList<>();
            authorityList.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role")));
            Authentication authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorityList);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }catch (Exception e){
        }
        //        다시 filterChain으로 돌아가는 로직
        chain.doFilter(request, response);
    }
}

