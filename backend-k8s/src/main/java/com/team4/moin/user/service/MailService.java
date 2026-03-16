package com.team4.moin.user.service;

import com.team4.moin.user.dtos.passwordupdatedtos.MailDto;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;  // 의존성 주입하면 스프링에서 알아서 야믈 파일에서 찾아줌

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(MailDto mailDto) {
        // 1. 메일 메시지 객체 생성
        SimpleMailMessage message = new SimpleMailMessage();

        // 2. DTO에 담긴 정보 세팅
        message.setTo(mailDto.getToEmail());   // 받는 사람
        message.setSubject(mailDto.getTitle()); // 제목
        message.setText(mailDto.getMessage());  // 본문 내용

        // 3. 실제 전송
        mailSender.send(message);
    }
}
