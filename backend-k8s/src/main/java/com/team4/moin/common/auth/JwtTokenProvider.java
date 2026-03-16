package com.team4.moin.common.auth;

import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secretKey}")
    private String st_secret_ket;
    @Value("${jwt.expiration}")
    private int expiration;

    @Value("${jwt.secretKeyRt}")
    private String st_secret_ket_rt;
    @Value("${jwt.expirationRt}")
    private int expirationRt;

    private Key secret_key_rt;

    private Key secret_key; // 선언해서

    private final RedisTemplate<String , String> redisTemplate;
    private final UserRepository userRepository;
    @Autowired
    public JwtTokenProvider(RedisTemplate<String, String> redisTemplate, UserRepository userRepository) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init(){
        secret_key = new SecretKeySpec(Base64.getDecoder().decode(st_secret_ket), SignatureAlgorithm.HS512.getJcaName());
        secret_key_rt = new SecretKeySpec(Base64.getDecoder().decode(st_secret_ket_rt), SignatureAlgorithm.HS512.getJcaName());
    }
    public String createToken(User user){

        Claims claims = Jwts.claims().setSubject(user.getEmail());
        claims.put("role", user.getRole().toString());

        Date now = new Date();


        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration*60*1000L)) // 30분*60초*1000밀리초 : 밀리초형태로 변환
                .signWith(secret_key)
                .compact();

        return token;
    }

    public String createRtToken(User user){
        Claims claims = Jwts.claims().setSubject(user.getEmail());
        claims.put("role", user.getRole().toString());
        Date now = new Date();


        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationRt*60*1000L)) //
                .signWith(secret_key_rt)
                .compact();
        redisTemplate.opsForValue().set(user.getEmail(), refreshToken, expirationRt, TimeUnit.MINUTES); //3000분 ttl
        return refreshToken;
    }

    public User validateRt(String refreshToken){
        Claims claims = null;

        try {
//        rt토큰 그 자체를 검증
            claims = Jwts.parserBuilder()
                    .setSigningKey(st_secret_ket_rt)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
        } catch (Exception e){
            throw new IllegalArgumentException("잘못 된 토큰입니다.");
        }
        String email = claims.getSubject();
        User user = userRepository.findAllByEmail(email).orElseThrow(()-> new EntityNotFoundException("entity not found"));

//        redis rt와 비교 겁증
        String redisRt = redisTemplate.opsForValue().get(email);
        if (!redisRt.equals(refreshToken)){
            throw new IllegalArgumentException("잘못 된 토큰입니다.");
        }

        return user;
    }
}