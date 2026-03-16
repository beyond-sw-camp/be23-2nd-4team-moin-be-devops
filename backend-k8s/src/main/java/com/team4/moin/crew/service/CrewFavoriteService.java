package com.team4.moin.crew.service;

import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.crew.domain.entitys.CrewFavorite;
import com.team4.moin.crew.repository.CrewFavoriteRepository;
import com.team4.moin.crew.repository.CrewRepository;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CrewFavoriteService {
    private final CrewFavoriteRepository crewFavoriteRepository;
    private final CrewRepository crewRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String,String>redisTemplate;

    @Autowired
    public CrewFavoriteService(CrewFavoriteRepository crewFavoriteRepository, CrewRepository crewRepository, UserRepository userRepository, @Qualifier("crewFavoriteInventory") RedisTemplate<String, String> redisTemplate) {
        this.crewFavoriteRepository = crewFavoriteRepository;
        this.crewRepository = crewRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }
    public void addFavorite(Long crewId) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
        Long userId = userRepository.findIdByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new EntityNotFoundException("없는 크루 입니다."));
        if ("Yes".equals(crew.getDelYn())) {
            throw new EntityNotFoundException("삭제된 크루 입니다.");
        }
        // 동시에 같은 찜 요청이 들어와 생기는 중복/락 예외는 정상 케이스라 무시
        try {
            int inserted = crewFavoriteRepository.insertIgnore(crewId, userId);
            if (inserted == 1) {
                crewRepository.incrementFavoriteCount(crewId);
            }
        }catch (TransientDataAccessException | DataIntegrityViolationException ignored){

        }
            //  Redis에 찜 상태 기록
            String key = "crew:fav:user:" + userId;
            redisTemplate.opsForSet().add(key, crewId.toString());
    }
    public void removeFavorite(Long crewId) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();
        Long userId = userRepository.findIdByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
        //  있으면 삭제, 없으면 0 리턴
        int deleted = crewFavoriteRepository.deleteByCrewIdAndUserId(crewId, userId);
        //  삭제가 실제로 됐을 때만 -1
        if (deleted > 0) {
            crewRepository.decrementFavoriteCount(crewId);
        }
            String key = "crew:fav:user:" + userId;
            redisTemplate.opsForSet().remove(key, crewId.toString());
    }
}
