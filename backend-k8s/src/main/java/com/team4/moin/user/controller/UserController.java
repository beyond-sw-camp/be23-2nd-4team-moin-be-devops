package com.team4.moin.user.controller;

import com.team4.moin.common.auth.JwtTokenProvider;
import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.dtos.*;
import com.team4.moin.user.dtos.logindtos.LoginUpdateDto;
import com.team4.moin.user.dtos.logindtos.RedirectDto;
import com.team4.moin.user.dtos.logindtos.UserLoginDto;
import com.team4.moin.user.dtos.mypage.MyPageResponseDto;
import com.team4.moin.user.dtos.passwordupdatedtos.CodeRequestDto;
import com.team4.moin.user.dtos.passwordupdatedtos.EmailRequestDto;
import com.team4.moin.user.dtos.passwordupdatedtos.PasswordUpdateDto;
import com.team4.moin.user.dtos.tokendtos.ResponseTokenDto;
import com.team4.moin.user.dtos.tokendtos.SocialTokenDto;
import com.team4.moin.user.dtos.tokendtos.TokenDto;
import com.team4.moin.user.repository.UserRepository;
import com.team4.moin.user.service.GoogleService;
import com.team4.moin.user.service.KakaoService;
import com.team4.moin.user.service.MyPageService;
import com.team4.moin.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleService googleService;
    private final KakaoService kakaoService;
    private final MyPageService myPageService;
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider, GoogleService googleService, KakaoService kakaoService, MyPageService myPageService, UserRepository userRepository) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.googleService = googleService;
        this.kakaoService = kakaoService;
        this.myPageService = myPageService;
        this.userRepository = userRepository;
    }


    @GetMapping("/presigned-url")
    public ResponseEntity<String> getPresignedUrl(@RequestParam String fileName) {
        String url = userService.getPresignedUrl(fileName);
        return ResponseEntity.ok(url);
    }
    @PostMapping("/create")
    public ResponseEntity<?> userCreate(
            @Valid @RequestBody UserSignupDto dto) {

        userService.create(dto);
        return new ResponseEntity<>("회원가입이 완료되었습니다.", HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> userLogin(@Valid @RequestBody UserLoginDto dto) {
        User user = userService.login(dto);
        String accessToken = jwtTokenProvider.createToken(user);
        String refreshToken = jwtTokenProvider.createRtToken(user);
        TokenDto returnToken = TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return new ResponseEntity<>(returnToken, HttpStatus.OK);
    }

    @PostMapping("/google/doLogin")
    public ResponseEntity<?> googleLogin(@RequestBody RedirectDto redirectDto) {

        User user = googleService.processGoogleLogin(redirectDto.getCode());
        boolean isNewUser = !user.isInfoCompleted();
        //  결과 유저를 바탕으로 우리 서비스용 at,rt  생성
        String accessToken = jwtTokenProvider.createToken(user);
        String refreshToken = jwtTokenProvider.createRtToken(user);
        SocialTokenDto returnToken = SocialTokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isNewUser(isNewUser)
                .build();
        System.out.println(returnToken);

        return new ResponseEntity<>(returnToken, HttpStatus.OK);

    }

    @PostMapping("/kakao/doLogin")
    public ResponseEntity<?> kakaoLogin(@RequestBody RedirectDto redirectDto) {
        User user = kakaoService.processKakaoLogin(redirectDto.getCode());

        boolean isNewUser = !user.isInfoCompleted();
        //  결과 유저를 바탕으로 우리 서비스용 at,rt  생성
        String accessToken = jwtTokenProvider.createToken(user);
        String refreshToken = jwtTokenProvider.createRtToken(user);
        SocialTokenDto returnToken = SocialTokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isNewUser(isNewUser)
                .build();
        System.out.println(returnToken);
        return new ResponseEntity<>(returnToken, HttpStatus.OK);
    }

    @PostMapping("/refresh-at")
    public ResponseEntity<?> refreshAt(@RequestBody TokenDto dto) {
//        rt 검증 (1.토큰 자체 검증 2. redis 조회 검증)
        User user = jwtTokenProvider.validateRt(dto.getRefreshToken());

//        at신규 생성
        String accessToken = jwtTokenProvider.createToken(user);
        ResponseTokenDto token = ResponseTokenDto.builder()
                .accessToken(accessToken)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        userService.logout();
        return ResponseEntity.ok("로그아웃되었습니다.");
    }

    //    소셜 로그인 후 추가 정보 입력
//        필수 입력 x 추가 정보 입력 화면 보내주고 Notnull X
    @PostMapping("/info-update")
    public ResponseEntity<?> updateInfo(@Valid @RequestBody LoginUpdateDto dto) {
        User user = userService.updateExtraInfo(dto);

        String accessToken = jwtTokenProvider.createToken(user);
        String refreshToken = jwtTokenProvider.createRtToken(user);

        TokenDto returnToken = TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return new ResponseEntity<>(returnToken, HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> softDelete() {
        userService.softDelete();

        return ResponseEntity.ok("회원 탈퇴가 정상적으로 처리되었습니다.");
    }


    @GetMapping("/info-from") // 기존 회원 정보 반환
    public ResponseEntity<?> getUserInfo() {
        UserUpdateDto dto = userService.getUserInfo();

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/data-update") // 데이터 변경
    public ResponseEntity<?> updateUserInfo(@RequestBody UserUpdateDto dto) {
        userService.updateUserInfo(dto);
        return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다.");
    }

    @PutMapping("/image-update") // 이미지로 변경
    public ResponseEntity<?> updateImageInfo(String profileImage) {
        userService.profileUpdate(profileImage);
        return ResponseEntity.ok("이미지가가 성공적으로 수정되었습니다.");
    }

    @PostMapping("/send-code")
    public ResponseEntity<?> sendResetCode(@RequestBody EmailRequestDto requestDto) {
        userService.passwordSendCode(requestDto.getEmail(),"PASSWORD");
        return ResponseEntity.ok("비밀번호 찾기 인증번호가 발송되었습니다.");
    }
    @PostMapping("/send-code-signup")
    public ResponseEntity<?> sendSignupCode(@RequestBody EmailRequestDto requestDto) {
        userService.createSendCode(requestDto.getEmail(), "SIGNUP");
        return ResponseEntity.ok("회원가입 인증번호가 발송되었습니다.");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody CodeRequestDto requestDto) {
        // 서비스에 DTO를 통째로 넘겨서 검증 결과를 boolean으로 받습니다.
        boolean isVerified = userService.verifyCode(requestDto);

        if (isVerified) {
            // 성공 시 200 OK와 메시지 반환
            return ResponseEntity.ok("인증에 성공하였습니다. 비밀번호를 변경해 주세요.");
        } else {
            // 실패 시 400 Bad Request와 에러 메시지 반환
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("인증번호가 일치하지 않거나 만료되었습니다.");
        }
    }

    @PatchMapping("/password-update")
    public ResponseEntity<String> updatePassword(@RequestBody PasswordUpdateDto requestDto) {
        userService.updatePassword(requestDto);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }

    @GetMapping("/my-page")
    public ResponseEntity<MyPageResponseDto> getMyPage() {
        MyPageResponseDto response = myPageService.getMyPageInfo();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my/id")
    public ResponseEntity<?> getMyUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findAllByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));
        return ResponseEntity.ok(user.getId());
    }
}
