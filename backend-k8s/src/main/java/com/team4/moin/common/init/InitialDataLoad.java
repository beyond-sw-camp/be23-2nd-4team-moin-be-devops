package com.team4.moin.common.init;

import com.team4.moin.user.domain.entitys.User;
import com.team4.moin.user.domain.enums.Role;
import com.team4.moin.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class InitialDataLoad implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialDataLoad(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if(userRepository.findAllByEmail("admin@naver.com").isPresent()){
            return;
        }
        userRepository.save(User.builder()
                .nickname("admin")
                .email("admin@naver.com")
                .password(passwordEncoder.encode("12341234"))
                .role(Role.ADMIN)
                .build());
    }

}
