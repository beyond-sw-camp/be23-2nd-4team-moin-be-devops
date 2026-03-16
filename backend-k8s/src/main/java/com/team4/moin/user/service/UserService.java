package com.team4.moin.user.service;

import com.team4.moin.common.auth.JwtTokenProvider;
import com.team4.moin.user.domain.enums.CategoryType;
import com.team4.moin.user.domain.entitys.Address;
import com.team4.moin.common.domain.Category;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.domain.enums.*;
import com.team4.moin.user.dtos.*;
import com.team4.moin.user.dtos.logindtos.GoogleProfileDto;
import com.team4.moin.user.dtos.logindtos.KakaoProfileDto;
import com.team4.moin.user.dtos.logindtos.LoginUpdateDto;
import com.team4.moin.user.dtos.logindtos.UserLoginDto;
import com.team4.moin.user.dtos.passwordupdatedtos.CodeRequestDto;
import com.team4.moin.user.dtos.passwordupdatedtos.MailDto;
import com.team4.moin.user.dtos.passwordupdatedtos.PasswordUpdateDto;
import com.team4.moin.user.repository.CategoryRepository;
import com.team4.moin.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final MailService mailService;
    private final RedisTemplate<String, String> emailInventoryTemplate;
    private final S3Client s3Client;
    private final RedisTemplate<String, String> redisTemplate;
    private final S3Presigner s3Presigner;
    @Value("${aws.s3.bucket1}")
    private String bucket;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CategoryRepository categoryRepository, MailService mailService, @Qualifier("emailInventory") RedisTemplate<String, String> emailInventoryTemplate, S3Client s3Client, @Qualifier("rtInventory") RedisTemplate<String, String> redisTemplate, S3Presigner s3Presigner) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.mailService = mailService;
        this.emailInventoryTemplate = emailInventoryTemplate;
        this.s3Client = s3Client;
        this.redisTemplate = redisTemplate;
        this.s3Presigner = s3Presigner;
    }

    public void create(UserSignupDto dto) {

//        String isVerified = emailInventoryTemplate.opsForValue().get("VERIFIED:" + dto.getEmail());
//        if (isVerified == null || !isVerified.equals("DONE")) {
//            throw new IllegalStateException("이메일 인증이 완료되지 않았습니다.");
//        }

        // 1. 이메일 중복 체크
        if (userRepository.findAllByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 2. 유저 엔티티 먼저 생성 및 저장
        User user = dto.toEntity(passwordEncoder.encode(dto.getPassword()));

//        // 3. S3 파일 업로드 및 URL 업데이트
        if (dto.getProfileImage() != null && !dto.getProfileImage().trim().isEmpty()) {
            user.createProfileImageUrl(dto.getProfileImage());
        }

        userRepository.save(user);


        // 4. 주소 엔티티 생성 및 연관관계 설정
        Address address = Address.builder()
                .city(dto.getCity())
                .district(dto.getDistrict())
                .street(dto.getStreet())
                .user(user)
                .build();

        user.updateAddress(address);
//        5. 카테고리 항목 리스트 생성 및 저장
        if (dto.getCategories() != null && !dto.getCategories().isEmpty()) {
            List<Category> categoryList = dto.getCategories().stream()
                    .map(catName -> Category.builder()
                            .categoryType(CategoryType.valueOf(catName.toUpperCase()))
                            .user(user)
                            .build())
                    .collect(Collectors.toList());
            categoryRepository.saveAll(categoryList);
        }
        emailInventoryTemplate.delete("VERIFIED:" + dto.getEmail());
    }

    public User login(UserLoginDto dto) {
        // 1. 이메일로 유저 조회
        // Optional을 사용하여 이메일이 없는 경우를 바로 처리
        User user = userRepository.findAllByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("email이 일치하지 않습니다."));

        // 2. 비밀번호 일치 확인
        // 조회된 유저의 암호화된 비밀번호와 입력받은 비밀번호를 대조.
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if ("Yes".equals(user.getDelYn())){
            throw new EntityNotFoundException("탈퇴 된 회원입니다.");
        }
        // 3. 검증 통과 시 유저 객체 반환
        return user;
    }

    public User getUserByProviderId(String providerId) {
        //  String providerId  조회
        User user = userRepository.findByProviderId(providerId).orElse(null);
        if (user == null) {
            return null;
        }
        if ("Yes".equals(user.getDelYn())){
            throw new EntityNotFoundException("탈퇴 된 회원입니다.");
        }
        return user;
    }

    // 구글로그인에서 회원정보 없을 때 생성
    public User createGoogleUser(GoogleProfileDto profileDto) {
        User user = User.builder()
                .email(profileDto.getEmail())
                .nickname(profileDto.getEmail().split("@")[0]) // 이메일을 닉네입으로 사용
                .provider(Provider.GOOGLE)
                .providerId(profileDto.getSub())
                .password(passwordEncoder.encode("OAUTH_USER_" + UUID.randomUUID()))
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }

    // 카카오로그인에서 회원정보 없을 때 생성
    public User createKakaoUser(KakaoProfileDto profileDto) {

        // 1. 기본값 설정 (선택 동의를 안 했을 경우 대비)
        String nickname = "익명사용자";

        // 2. 카카오 계정 정보가 있고, 그 안에 프로필 객체가 존재하는지 확인
        if (profileDto.getKakao_account() != null && profileDto.getKakao_account().getProfile() != null) {
            String kakaoNickname = profileDto.getKakao_account().getProfile().getNickname();
            // 닉네임 문자열 자체가 비어있지 않은지도 확인하면 더 좋습니다.
            if (kakaoNickname != null && !kakaoNickname.isEmpty()) {
                nickname = kakaoNickname;
            }
        }

        User user = User.builder()
                .email(profileDto.getKakao_account().getEmail())
                .nickname(nickname)
                .provider(Provider.KAKAO)
                .providerId(profileDto.getId())
                .password(passwordEncoder.encode("OAUTH_USER_" + UUID.randomUUID()))
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }

    public User updateExtraInfo(LoginUpdateDto dto) {

        User user = userRepository.findAllByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Mbti mbti = Optional.ofNullable(dto.getMbti())
                .filter(s -> !s.isBlank())
                .map(String::toUpperCase)
                .map(Mbti::valueOf)
                .orElse(null);

        if (dto.getProfileImage() != null && !dto.getProfileImage().trim().isEmpty()) {
            user.createProfileImageUrl(dto.getProfileImage());
        }
        // 1. 유저 정보 업데이트

        user.updateAdditionalInfo(
                dto.getBirthDate(),
                Gender.valueOf(dto.getGender().toUpperCase()), //이넘타입 문자열로 변환 db에 대문자로 저장
                dto.getNickname(),
                mbti
        );

        // 2. 주소 저장
        Address address = Address.builder()
                .city(dto.getCity())
                .district(dto.getDistrict())
                .street(dto.getStreet())
                .user(user)
                .build();
        user.updateAddress(address); // cascade 사용 save X

        // 3. 카테고리 저장
        if (dto.getCategories() != null && !dto.getCategories().isEmpty()) {
            List<Category> categoryList = dto.getCategories().stream()
                    .map(catName -> Category.builder()
                            .categoryType(CategoryType.valueOf(catName.toUpperCase()))
                            .user(user)
                            .build())
                    .collect(Collectors.toList());
            categoryRepository.saveAll(categoryList);
        }

        return user;
    }

    @Transactional
    public void softDelete() {
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다."));

        redisTemplate.delete(email); //rt 함께 삭제


        user.updateDelete();


    }
    @Transactional
    public void logout() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
            redisTemplate.delete(email);
    }
    @Transactional(readOnly = true)
    public UserUpdateDto getUserInfo(){

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
        UserUpdateDto dto = UserUpdateDto.fromEntity(user);


        return dto;
    }
    // 정보 수정 로직
    public void updateUserInfo(UserUpdateDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        // 1. 기본 필드 업데이트 (User 엔티티 내에 메서드 생성 권장)
        user.updateDataInfo(dto);
        // 2. 주소 업데이트 (Address 객체 null 체크 필수)
        if (user.getAddress() != null) {
            user.getAddress().updateAddress(dto.getCity(), dto.getStreet(), dto.getDistrict());
        }
        // 3. 카테고리 업데이트 (기존 리스트 비우고 새로 추가)
        if (dto.getCategoryNames() != null) {
            user.getCategories().clear(); // orphanRemoval = true 설정 확인 필수
            for (String categoryName : dto.getCategoryNames()) {
                CategoryType type = CategoryType.valueOf(categoryName.toUpperCase());
                Category newCategory = Category.builder()
                        .categoryType(type)
                        .user(user)
                        .build();
                user.getCategories().add(newCategory);
            }
        }
    }
    public void profileUpdate(String profileImage) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findAllByEmailWithAddress(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        String oldProfileImageUrl = user.getProfileImageUrl();
            // 1. 기존 이미지가 있다면 S3에서 먼저 삭제
            if (oldProfileImageUrl != null && !oldProfileImageUrl.equals(profileImage)) {
                try {
                    String oldFileName = oldProfileImageUrl.substring(oldProfileImageUrl.lastIndexOf("/") + 1);
                    s3Client.deleteObject(a -> a.bucket(bucket).key(oldFileName));
                } catch (Exception e) {
                    // S3에 이미 파일이 없는 경우 등 예외 발생 시 무시하거나 로그만 남김
                    System.out.println("기존 프로필 이미지 삭제 실패 (이미 존재하지 않을 수 있음): " + e.getMessage());
                }
            }

            // 2. 새로운 이미지를 올린 경우 (DB URL 업데이트)
            if (profileImage != null && !profileImage.trim().isEmpty()) {
                user.updateProfileImageUrl(profileImage);
            }
            // 요청 없을 땐 null
            else {
                user.updateProfileImageUrl(null);
            }
    }

    public void createSendCode(String email, String type) {

        userRepository.findAllByEmailWithAddress(email)
                .ifPresent(u -> { throw new IllegalArgumentException("이미 존재하는 이메일입니다."); });

        // 1. 난수 생성 6자리 랜덤
        String code = String.valueOf((int)(Math.random() * 899999) + 100000);

        // 2. 데이터 저장
        // set(Key, Value, 유효시간, 시간단위)
        emailInventoryTemplate.opsForValue().set("AUTH:" + email, code, 5, TimeUnit.MINUTES); //ttl 5분

        // 3. 메일 발송
        MailDto mailDto;
        if ("SIGNUP".equals(type)) {
            // 회원가입용 DTO 객체 1개만 생성 (forSignup 실행)
            mailDto = MailDto.forSignup(email, code);
        } else {
            // 비밀번호용 DTO 객체 1개만 생성 (from 실행)
            mailDto = MailDto.from(email, code);
        }
        mailService.sendEmail(mailDto);
    }
    public void passwordSendCode(String email, String type) {

        // 1. 난수 생성 6자리 랜덤
        String code = String.valueOf((int)(Math.random() * 899999) + 100000);

        // 2. 데이터 저장
        // set(Key, Value, 유효시간, 시간단위)
        emailInventoryTemplate.opsForValue().set("AUTH:" + email, code, 5, TimeUnit.MINUTES); //ttl 5분

        // 3. 메일 발송
        MailDto mailDto;
        if ("SIGNUP".equals(type)) {
            // 회원가입용 DTO 객체 1개만 생성 (forSignup 실행)
            mailDto = MailDto.forSignup(email, code);
        } else {
            // 비밀번호용 DTO 객체 1개만 생성 (from 실행)
            mailDto = MailDto.from(email, code);
        }
        mailService.sendEmail(mailDto);
    }
    public boolean verifyCode(CodeRequestDto dto) {

        String email = dto.getEmail();
        String code = dto.getCode();

        String savedCode = emailInventoryTemplate.opsForValue().get("AUTH:" + email); // redis에 저장된 난수 값

        if (savedCode == null) { // 인증 시간이 지나면 false
            return false;
        }
        if (savedCode.equals(code)) { // 인증번호와 비교
            emailInventoryTemplate.delete("AUTH:" + email); // 난수값 삭제

            //  회원가입 시 확인을 위해 인증 완료 상태를 Redis에 10분간 저장
            emailInventoryTemplate.opsForValue().set("VERIFIED:" + email, "DONE", 10, TimeUnit.MINUTES);

            return true; // 성공하면 true
        }
        return false; //다르면 false
    }
    @Transactional
    public void updatePassword(PasswordUpdateDto dto) {

        User user = userRepository.findAllByEmailWithAddress(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getDelYn().equals("Yes")){
            throw new IllegalArgumentException("탈퇴 된 회원입니다.");
        }

        String encodedPassword = passwordEncoder.encode(dto.getNewPassword());


        user.updatePassword(encodedPassword);
    }
    public String getPresignedUrl(String originalFileName) {

        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = "user" + "_" + System.currentTimeMillis() + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5)) // URL 유효시간 5분
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }
    }

