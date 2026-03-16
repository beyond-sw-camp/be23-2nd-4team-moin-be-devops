package com.team4.moin.common.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.team4.moin.common.dto.CommonErrorDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegal(IllegalArgumentException e){
        e.printStackTrace();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(400)
                .error_message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> notValidException(MethodArgumentNotValidException e){
        e.printStackTrace();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(400)
                .error_message(e.getFieldError().getDefaultMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> noSuchElement(NoSuchElementException e){
        e.printStackTrace();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(404)
                .error_message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(dto);
    }
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> illegalState(IllegalStateException e){
        e.printStackTrace();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(400)
                .error_message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> entityNot(EntityNotFoundException e){
        e.printStackTrace();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(404)
                .error_message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(dto);
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> entityNot(RuntimeException e){
        if (e instanceof org.springframework.security.authorization.AuthorizationDeniedException) {
            return null; // SSE 비동기 dispatch 시 발생하는 Access Denied 무시
        }
        e.printStackTrace();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(500)
                .error_message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<?> jsonProcessingException(JsonProcessingException e){
        e.printStackTrace();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(400)
                .error_message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }

//    @ExceptionHandler(AuthorizationDeniedException.class)
//    public ResponseEntity<?> noSuchElement(AuthorizationDeniedException e){
//        e.printStackTrace();
//        CommonErrorDto dto = CommonErrorDto.builder()
//                .status_code(403)
//                .error_message(e.getMessage())
//                .build();
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(dto);
//    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> exception(Exception e, HttpServletRequest request) {
        // SSE 연결에서 발생한 IOException은 무시 (클라이언트가 연결 끊은 것)
        if (e instanceof java.io.IOException) {
            return null;
        }
        if (e instanceof org.springframework.web.context.request.async.AsyncRequestTimeoutException) {
            return null; // SSE 타임아웃 정상 상황
        }
        e.printStackTrace();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(500)
                .error_message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto);
    }
}
