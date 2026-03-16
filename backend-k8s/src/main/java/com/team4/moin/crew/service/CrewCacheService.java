package com.team4.moin.crew.service;

import com.team4.moin.common.domain.Category;
import com.team4.moin.crew.domain.entitys.Crew;
import com.team4.moin.crew.domain.entitys.CrewFavorite;
import com.team4.moin.crew.dtos.CrewListDto;
import com.team4.moin.crew.repository.CrewFavoriteRepository;
import com.team4.moin.crew.repository.CrewRepository;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.domain.enums.CategoryType;
import com.team4.moin.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CrewCacheService {

    private final CrewRepository crewRepository;
    private final CrewFavoriteRepository crewFavoriteRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> crewFavoriteRedis;

    public CrewCacheService(CrewRepository crewRepository, CrewFavoriteRepository crewFavoriteRepository, UserRepository userRepository, @Qualifier("crewFavoriteInventory") RedisTemplate<String, String> crewFavoriteRedis) {
        this.crewRepository = crewRepository;
        this.crewFavoriteRepository = crewFavoriteRepository;
        this.userRepository = userRepository;
        this.crewFavoriteRedis = crewFavoriteRedis;
    }

    //      맞춤 추천 크루 (로그인 유저용) ///
    @Cacheable(value = "crewRecommendation", key = "#email")
    public List<CrewListDto> getRecommendedCrewsCache(String email) {
        User user = (email != null && !email.equals("anonymousUser"))
                ? userRepository.findAllByEmail(email).orElse(null) : null;

        Page<Crew> crewPage;
        // 로그인 유저: 지역/관심사 기반 추천
        if (user != null) {
            String userDistrict = (user.getAddress() != null) ? user.getAddress().getDistrict() : "";
            List<CategoryType> userCategories = user.getCategories().stream()
                    .map(Category::getCategoryType)
                    .collect(Collectors.toList());
            if (userCategories.isEmpty()) userCategories.add(null);
            crewPage = crewRepository.findRecommendedCrews(userDistrict, userCategories, Pageable.unpaged());
        }
        //  비로그인 유저: 인기순 추천
        else {
            crewPage = crewRepository.findPopularCrews(Pageable.unpaged());
        }

        List<CrewListDto> dtoList = crewPage.getContent().stream()
                .map(CrewListDto::fromEntity)
                .collect(Collectors.toList());

        if (user != null) {
            List<CrewFavorite> favoriteList = crewFavoriteRepository.findAllByUserIdWithCrew(user.getId());
            List<Long> favoriteCrewIds = favoriteList.stream()
                    .map(cf -> cf.getCrew().getId())
                    .collect(Collectors.toList());

            dtoList.forEach(dto -> dto.setFavorite(favoriteCrewIds.contains(dto.getCrewId())));
        }
        return dtoList;
    }


    //     급상승 루키 크루 페이징 조회 (최근 14일 생성 + 조회수 순)
    @Cacheable(value = "crewSoaring", key = "#email")
    public List<CrewListDto> getSoaringCrewsCache(String email) {
        LocalDateTime fourteenDaysAgo = LocalDateTime.now().minusDays(14);

        Page<Crew> crewPage = crewRepository.findRookieCrews(fourteenDaysAgo, Pageable.unpaged());

        List<CrewListDto> dtoList = crewPage.stream().
                map(CrewListDto::fromEntity)
                .collect(Collectors.toList());

        User user = (email != null && !email.equals("anonymousUser"))
                ? userRepository.findAllByEmail(email).orElse(null) : null;

        if (user != null) {
            List<CrewFavorite> favoriteList = crewFavoriteRepository.findAllByUserIdWithCrew(user.getId());
            List<Long> favoriteCrewIds = favoriteList.stream()
                    .map(cf -> cf.getCrew().getId())
                    .collect(Collectors.toList());


            for (CrewListDto dto : dtoList) {
                dto.setFavorite(favoriteCrewIds.contains(dto.getCrewId()));
            }
        }
        return dtoList;
    }


    //    인기순 크루 조회
    @Cacheable(value = "popularCrews", key = "'popular'")
    public List<CrewListDto> getPopularCrewsCache() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "viewCount"));
        Page<Crew> crewPage = crewRepository.findAllByDelYn("No", pageable);

        return crewPage.getContent().stream()
                .map(CrewListDto::fromEntity)
                .collect(Collectors.toList());
    }



}
