package com.team4.moin.common.service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Slf4j
@Service
public class PortOneService {

    private final RestTemplate restTemplate;

    @Value("${portone.api-secret}")
    private String apiSecret;

    public PortOneService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


//      포트원 V2 단건 조회 (V2 전용 API 주소)

    public Long getActualPaymentAmount(String paymentId) {
        String url = "https://api.portone.io/payments/" + paymentId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne " + apiSecret.trim());

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        Map<String, Object> body = response.getBody();
        Map<String, Object> amountMap = (Map<String, Object>) body.get("amount");
        return Long.parseLong(amountMap.get("total").toString());
    }

//      포트원 V2 결제 취소(환불)
    public void cancelPayment(String paymentId, Long amount, String reason) {
        String url = "https://api.portone.io/payments/" + paymentId + "/cancel";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne " + apiSecret.trim());
        headers.set("Content-Type", "application/json");

        // 취소 요청 바디 구성
        Map<String, Object> body = Map.of(
                "amount", amount,
                "reason", reason
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            log.info("포트원 V2 취소 성공: paymentId={}, 응답={}", paymentId, response.getBody());
        } catch (Exception e) {
            log.error("포트원 V2 취소 실패: paymentId={}, 에러={}", paymentId, e.getMessage());
            throw new IllegalStateException("결제 취소 처리 중 오류가 발생했습니다.");
        }
    }
}