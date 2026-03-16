package com.team4.moin.meeting.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team4.moin.meeting.dtos.MeetingMapDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TmapService {
    @Value("${tmap.appKey}")
    private String appKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TmapService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // 사용자가 입력한 place를 최종 좌표로 바꾸는 메서드
    public Coordinate getCoordinateFromPlace(String place) {
        // 입력이 비어있으면 바로 에러
        if (place == null || place.trim().isEmpty()) {
            throw new IllegalArgumentException("장소가 비어있습니다.");
        }
        String input = place.trim();
        // 주소처럼 보이면 주소 지오코딩으로 바로 좌표 확정
        if (looksLikeAddress(input)) {
            Coordinate geo = tryFullTextGeocoding(input);
            if (geo != null) return geo;
            throw new IllegalArgumentException("주소를 찾을 수 없습니다: " + input);
        }
        // 주소가 아니면(역/상호명 등) 자동 확정하지 않고 후보 선택 유도
        throw new IllegalArgumentException("후보 선택 필요: " + input);
    }
    // 내부 후보를 프론트 응답용으로 바꿔서 반환
    public List<MeetingMapDto> searchCandidates(String query) {
        List<PoiCandidate> candidates = searchPoiCandidates(query);
        if (candidates.isEmpty()) return List.of();

        List<MeetingMapDto> out = new ArrayList<>();
        for (PoiCandidate c : candidates) {
            out.add(MeetingMapDto.builder()
                    .name(c.getName())
                    .address(c.getAddress())
                    .latitude(c.getLatitude())
                    .longitude(c.getLongitude())
                    .build());
        }
        return out;
    }

    // 키워드로 장소 후보 목록을 가져옴
    public List<PoiCandidate> searchPoiCandidates(String query) {
        // 검색어가 없으면 빈 리스트
        if (query == null || query.trim().isEmpty()) return List.of();
        // 티맵 POI 검색 요청 주소 만들기
        var uri = UriComponentsBuilder
                .fromHttpUrl("https://apis.openapi.sk.com/tmap/pois")
                .queryParam("version", "1")
                .queryParam("searchKeyword", query.trim())
                .queryParam("count", "5")
                .queryParam("reqCoordType", "WGS84GEO")
                .queryParam("resCoordType", "WGS84GEO")
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        // 요청 헤더에 앱키 넣고 JSON 응답 받도록 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("appKey", appKey == null ? "" : appKey.trim());
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        // 티맵 API 호출
        ResponseEntity<String> res = restTemplate.exchange(
                uri, HttpMethod.GET, new HttpEntity<>(headers), String.class
        );
        // 검색 결과가 없으면 빈 리스트
        if (res.getStatusCode() == HttpStatus.NO_CONTENT) return List.of();
        if (!res.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("TMAP POI 실패 status=" + res.getStatusCode() + " body=" + res.getBody());
        }
        String raw = res.getBody();
        // 본문이 비어있으면 빈 리스트
        if (raw == null || raw.isBlank()) return List.of();
        try {
            // JSON 파싱
            JsonNode root = objectMapper.readTree(raw);
            // 실제 후보 데이터가 들어있는 위치
            JsonNode poiNode = root.path("searchPoiInfo").path("pois").path("poi");
            if (poiNode.isMissingNode() || poiNode.isNull()) return List.of();
            // 장소명 검색결과가 배열일 수도 1개 객체일 수도 있어서 리스트로 통일
            List<JsonNode> poiItems = new ArrayList<>();
            if (poiNode.isArray()) {
                poiNode.forEach(poiItems::add);
            } else if (poiNode.isObject()) {
                poiItems.add(poiNode);
            } else {
                return List.of();
            }

            List<PoiCandidate> out = new ArrayList<>();

            // 후보 하나씩 읽어서 우리 객체로 변환
            for (JsonNode p : poiItems) {
                // 위도 후보값 중 첫 번째 유효값
                String latStr = firstNonBlank(
                        textOrNull(p, "frontLat"),
                        textOrNull(p, "noorLat"),
                        textOrNull(p, "lat"),
                        textOrNull(p, "newLat")
                );

                // 경도 후보값 중 첫 번째 유효값
                String lonStr = firstNonBlank(
                        textOrNull(p, "frontLon"),
                        textOrNull(p, "noorLon"),
                        textOrNull(p, "lon"),
                        textOrNull(p, "newLon")
                );

                // 좌표가 없으면 이 후보는 제외
                if (latStr == null || lonStr == null) continue;

                // 장소 이름
                String name = firstNonBlank(textOrNull(p, "name"), "");

                // 도로명 주소 조립
                String roadAddress = joinNonBlank(
                        textOrNull(p, "upperAddrName"),
                        textOrNull(p, "middleAddrName"),
                        textOrNull(p, "lowerAddrName"),
                        textOrNull(p, "roadName"),
                        buildBuildingNo(textOrNull(p, "firstBuildNo"), textOrNull(p, "secondBuildNo"))
                );

                // 지번 주소 조립
                String jibunAddress = joinNonBlank(
                        textOrNull(p, "upperAddrName"),
                        textOrNull(p, "middleAddrName"),
                        textOrNull(p, "lowerAddrName"),
                        buildBuildingNo(textOrNull(p, "firstNo"), textOrNull(p, "secondNo"))
                );

                // 도로명 주소 우선, 없으면 지번 주소 사용
                String address = firstNonBlank(roadAddress, jibunAddress, "");

                // 후보 리스트에 추가
                out.add(new PoiCandidate(
                        name,
                        address,
                        Double.parseDouble(latStr),
                        Double.parseDouble(lonStr)
                ));
            }

            return out;
        } catch (Exception e) {
            throw new IllegalStateException("TMAP POI JSON 파싱 실패 raw=" + raw, e);
        }
    }

    // 정확한 주소를 좌표 1개로 바꾸는 메서드
    private Coordinate tryFullTextGeocoding(String fullAddr) {
        // 티맵 주소 지오코딩 요청 주소 만들기
        var uri = UriComponentsBuilder
                .fromHttpUrl("https://apis.openapi.sk.com/tmap/geo/fullAddrGeo")
                .queryParam("version", "1")
                .queryParam("format", "json")
                .queryParam("coordType", "WGS84GEO")
                .queryParam("addressFlag", "F01")
                .queryParam("page", "1")
                .queryParam("count", "1")
                .queryParam("fullAddr", fullAddr.trim())
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("appKey", appKey == null ? "" : appKey.trim());
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<String> res;
        try {
            // 티맵 API 호출
            res = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        } catch (HttpStatusCodeException e) {
            // 주소를 못 찾으면 null 반환
            return null;
        }

        // 결과 없거나 비정상 코드면 null
        if (res.getStatusCode() == HttpStatus.NO_CONTENT) return null;
        if (!res.getStatusCode().is2xxSuccessful()) return null;

        String raw = res.getBody();
        if (raw == null || raw.isBlank()) return null;

        try {
            // JSON 파싱
            JsonNode root = objectMapper.readTree(raw);
            JsonNode coordArr = root.path("coordinateInfo").path("coordinate");
            if (coordArr.isMissingNode() || coordArr.isNull()) return null;

            // 배열이면 첫 번째 좌표 사용
            JsonNode first = (coordArr.isArray() && coordArr.size() > 0) ? coordArr.get(0) : coordArr;
            if (first == null || first.isNull()) return null;

            // 위도/경도 읽기
            String lat = firstNonBlank(textOrNull(first, "newLat"), textOrNull(first, "lat"));
            String lon = firstNonBlank(textOrNull(first, "newLon"), textOrNull(first, "lon"));
            if (lat == null || lon == null) return null;

            return new Coordinate(Double.parseDouble(lat), Double.parseDouble(lon));
        } catch (Exception e) {
            return null;
        }
    }

    // 입력 문자열이 주소처럼 보이는지 간단히 체크
    private boolean looksLikeAddress(String input) {
        if (input == null) return false;
        String s = input.trim();

        // 역/카페/공원 같은 키워드는 주소 아님
        if (s.matches(".*(역|공원|학교|병원|카페|빌딩|타워|터미널|마트|백화점|아파트).*")) {
            return false;
        }

        // 숫자 + 주소 단어가 같이 있으면 주소로 판단
        boolean hasNumber = s.matches(".*\\d+(-\\d+)?\\s*.*");
        boolean hasAddressHint = s.matches(".*(로|길|대로|동|리|가|번길|번지).*");
        return hasNumber && hasAddressHint;
    }

    // JSON에서 key 값을 문자열로 안전하게 꺼냄
    private String textOrNull(JsonNode node, String key) {
        JsonNode v = node.get(key);
        if (v == null || v.isNull()) return null;
        String s = v.asText();
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    // 여러 문자열 중 비어있지 않은 첫 값 반환
    private String firstNonBlank(String... arr) {
        for (String s : arr) {
            if (s != null && !s.isBlank()) return s;
        }
        return null;
    }

    // null/빈값은 빼고 공백으로 이어붙임
    private String joinNonBlank(String... arr) {
        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            if (s == null || s.isBlank()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(s.trim());
        }
        return sb.toString();
    }

    // 번지 숫자 조합 (예: 123-45)
    private String buildBuildingNo(String firstNo, String secondNo) {
        if (firstNo == null) return null;
        if (secondNo == null || secondNo.isBlank() || "0".equals(secondNo)) return firstNo;
        return firstNo + "-" + secondNo;
    }

    // 좌표를 담는 작은 객체
    // latitude = 위도, longitude = 경도
    public static class Coordinate {
        private final double latitude;
        private final double longitude;

        public Coordinate(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }

    // 장소 후보 1개를 담는 작은 객체
    // name = 장소명, address = 주소, latitude/longitude = 좌표
    public static class PoiCandidate {
        private final String name;
        private final String address;
        private final double latitude;
        private final double longitude;

        public PoiCandidate(String name, String address, double latitude, double longitude) {
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getName() { return name; }
        public String getAddress() { return address; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }
}
