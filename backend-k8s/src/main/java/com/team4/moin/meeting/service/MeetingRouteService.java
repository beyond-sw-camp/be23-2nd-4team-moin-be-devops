package com.team4.moin.meeting.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team4.moin.meeting.domain.entitys.Meeting;
import com.team4.moin.meeting.domain.enums.RouteMode;
import com.team4.moin.meeting.dtos.route.MeetingRouteReqDto;
import com.team4.moin.meeting.dtos.route.MeetingRouteResDto;
import com.team4.moin.meeting.dtos.route.RouteStepDto;
import com.team4.moin.meeting.repository.MeetingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class MeetingRouteService {

    private final MeetingRepository meetingRepository;

    @Value("${tmap.appKey}")
    private String appKey;

    private final RestTemplate rt = new RestTemplate();

    private final ObjectMapper om = new ObjectMapper();

    @Autowired
    public MeetingRouteService(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    public String routeRaw(MeetingRouteReqDto dto) {

        Meeting meeting = meetingRepository.findByIdAndCrew_Id(dto.getMeetingId(), dto.getCrewId())
                .orElseThrow(() -> new EntityNotFoundException("없는 모임입니다."));

        // DB에 저장된 목적지 좌표
        Double endLat = meeting.getLatitude();
        Double endLng = meeting.getLongitude();

        // 목적지 좌표 없으면 길찾기 불가
        if (endLat == null || endLng == null) {
            throw new IllegalStateException("모임 좌표가 없습니다.");
        }

        // 출발지 좌표(내 위치)는 프론트에서 받음
        double startLat = dto.getStartLat();
        double startLng = dto.getStartLng();

        //  모드별 API 호출
        if (dto.getMode() == RouteMode.CAR) {
            return callCarRaw(startLat, startLng, endLat, endLng);
        }
        if (dto.getMode() == RouteMode.WALK) {
            return callWalkRaw(startLat, startLng, endLat, endLng);
        }
        return callTransitRaw(startLat, startLng, endLat, endLng);
    }

    public MeetingRouteResDto routeSummary(MeetingRouteReqDto dto) {

        String raw = routeRaw(dto);

        //  대중교통은 구조가 달라서 따로 파싱
        if (dto.getMode() == RouteMode.TRANSIT) {
            return parseTransitSummary(raw);
        }

        return parseTmapFeatureBased(raw);
    }

    //   차량호출
    private String callCarRaw(double slat, double slng, double elat, double elng) {
        String url = "https://apis.openapi.sk.com/tmap/routes?version=1&format=json";

        //  요청 body 만들기
        Map<String, Object> body = new HashMap<>();
        body.put("startX", slng);
        body.put("startY", slat);
        body.put("endX", elng);
        body.put("endY", elat);
        body.put("reqCoordType", "WGS84GEO");
        body.put("resCoordType", "WGS84GEO");
        body.put("searchOption", "0");
        body.put("trafficInfo", "N");

        return postJson(url, body);
    }

    //  도보호출
    private String callWalkRaw(double slat, double slng, double elat, double elng) {
        String url = "https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&format=json";

        // 요청 body 만들기
        Map<String, Object> body = new HashMap<>();
        body.put("startX", slng);
        body.put("startY", slat);
        body.put("endX", elng);
        body.put("endY", elat);
        body.put("reqCoordType", "WGS84GEO");
        body.put("resCoordType", "WGS84GEO");
        body.put("startName", "start");
        body.put("endName", "end");

        return postJson(url, body);
    }

    //    대중교통 호출
    private String callTransitRaw(double slat, double slng, double elat, double elng) {
        String url = "https://apis.openapi.sk.com/transit/routes";

        // 대중교통도 숫자로 보내는 게 가장 안전함
        Map<String, Object> body = new HashMap<>();
        body.put("startX", slng);
        body.put("startY", slat);
        body.put("endX", elng);
        body.put("endY", elat);
        body.put("count", 1);
        body.put("lang", 0);
        body.put("format", "json");

        return postJson(url, body);
    }

    private String postJson(String url, Map<String, Object> reqBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("appKey", appKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(reqBody, headers);

        try {
            ResponseEntity<String> res = rt.exchange(url, HttpMethod.POST, entity, String.class);

            if (!res.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("TMAP 호출 실패 status=" + res.getStatusCode() + " body=" + res.getBody());
            }
            if (res.getBody() == null || res.getBody().isBlank()) {
                throw new IllegalStateException("TMAP 응답이 없습니다.");
            }
            return res.getBody();

        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            // TMAP이 준 에러 바디까지 같이 보여주기(원인 파악용)
            throw new IllegalStateException(
                    "TMAP 호출 실패 status=" + e.getStatusCode() + " body=" + e.getResponseBodyAsString(), e
            );
        }
    }

    private MeetingRouteResDto parseTmapFeatureBased(String raw) {

        Map<String, Object> root = readJson(raw);
        List<Map<String, Object>> features = asListOfMap(root.get("features"));

        int totalDistance = 0;
        int totalTime = 0;
        List<double[]> path = new ArrayList<>();

        if (features != null) {
            for (Map<String, Object> f : features) {

                Map<String, Object> props = asMap(f.get("properties"));
                if (props != null) {
                    Integer td = asInt(props.get("totalDistance"));
                    Integer tt = asInt(props.get("totalTime"));
                    if (td != null) totalDistance = Math.max(totalDistance, td);
                    if (tt != null) totalTime = Math.max(totalTime, tt);
                }

                Map<String, Object> geom = asMap(f.get("geometry"));
                if (geom == null) continue;

                String type = asString(geom.get("type"));
                if (!"LineString".equalsIgnoreCase(type)) continue;

                List<?> coords = (List<?>) geom.get("coordinates");
                if (coords == null) continue;

                for (Object c : coords) {
                    if (!(c instanceof List<?> cl) || cl.size() < 2) continue;
                    Double lng = asDouble(cl.get(0));
                    Double lat = asDouble(cl.get(1));
                    if (lat == null || lng == null) continue;
                    path.add(new double[]{lat, lng});
                }
            }
        }

        return MeetingRouteResDto.builder()
                .distance(totalDistance)
                .time(totalTime)
                .path(path)
                .steps(Collections.emptyList())
                .transfers(0)
                .fare(0)
                .build();
    }

    private MeetingRouteResDto parseTransitSummary(String raw) {

        Map<String, Object> root = readJson(raw);

        Map<String, Object> meta = asMap(root.get("metaData"));
        if (meta == null) meta = root;

        Map<String, Object> plan = asMap(meta.get("plan"));
        if (plan == null) plan = meta;

        List<Map<String, Object>> itineraries = asListOfMap(plan.get("itineraries"));

        // 경로가 아예 없을 때(가까운 거리/서비스 제한)
        if (itineraries == null || itineraries.isEmpty()) {
            return MeetingRouteResDto.builder()
                    .distance(0)
                    .time(0)
                    .path(Collections.emptyList())
                    .steps(Collections.emptyList())
                    .transfers(0)
                    .fare(0)
                    .build();
        }

        // 첫번째 경로 사용(count=1)
        Map<String, Object> it0 = itineraries.get(0);

        Integer totalDistance = asInt(it0.get("totalDistance"));
        Integer totalTime = asInt(it0.get("totalTime"));

        // 요금 파싱(있을 때만)
        Integer fare = null;
        Map<String, Object> fareMap = asMap(it0.get("fare"));
        if (fareMap != null) {
            Map<String, Object> regular = asMap(fareMap.get("regular"));
            if (regular != null) fare = asInt(regular.get("totalFare"));
            if (fare == null) fare = asInt(fareMap.get("totalFare"));
        }

        // legs 파싱(도보/버스/지하철 구간들)
        List<Map<String, Object>> legs = asListOfMap(it0.get("legs"));
        List<RouteStepDto> steps = new ArrayList<>();

        int transitLegCount = 0; //  BUS/SUBWAY 구간 개수(환승 계산용)

        if (legs != null) {
            for (Map<String, Object> leg : legs) {

                String mode = asString(leg.get("mode"));
                Integer dist = asInt(leg.get("distance"));
                Integer sec = asInt(leg.get("sectionTime"));

                String fromName = null;
                String toName = null;

                // 출발/도착 이름(정류장/역) 뽑기
                Map<String, Object> start = asMap(leg.get("start"));
                if (start == null) start = asMap(leg.get("from")); // start가 없으면 from을 본다
                if (start != null) fromName = asString(start.get("name"));

                Map<String, Object> end = asMap(leg.get("end"));
                if (end == null) end = asMap(leg.get("to")); // end가 없으면 to를 본다
                if (end != null) toName = asString(end.get("name"));

                // 노선명(버스번호/호선명)
                String routeRaw = extractTransitRouteName(leg);
                String route = normalizeTransitRouteLabel(mode, routeRaw); // 보기 좋게 가공

                // 정거장 수(필드명이 다를 수 있어 2개 시도)
                Integer stations = asInt(leg.get("stationCount"));
                if (stations == null) stations = asInt(leg.get("passStopCount"));

                if ("BUS".equalsIgnoreCase(mode) || "SUBWAY".equalsIgnoreCase(mode)) {
                    transitLegCount++;
                }

                steps.add(RouteStepDto.builder()
                        .mode(mode)
                        .route(route)
                        .fromName(fromName)
                        .toName(toName)
                        .distance(dist)
                        .time(sec)
                        .stations(stations)
                        .build());
            }
        }

        //  환승 횟수: transferCount 있으면 그걸 쓰고, 없으면 (BUS/SUBWAY 구간수 - 1)
        Integer transferCount = asInt(it0.get("transferCount"));
        int transfers = (transferCount != null)
                ? transferCount
                : Math.max(0, transitLegCount - 1);

        return MeetingRouteResDto.builder()
                .distance(totalDistance != null ? totalDistance : 0)
                .time(totalTime != null ? totalTime : 0)
                .path(Collections.emptyList())
                .steps(steps)
                .transfers(transfers)
                .fare(fare != null ? fare : 0)
                .build();
    }

    //      노선명 뽑기
    //     BUS/SUBWAY에서 "몇번 버스/몇호선" 같은 문자열을 최대한 뽑아냄
    private String extractTransitRouteName(Map<String, Object> leg) {
        Object routeObjRaw = leg.get("route");

        // route가 list로 오는 경우가 있어서 먼저 처리
        if (routeObjRaw instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);

            // list 안 원소가 Map이면 routeName/name/number 순서로 찾아본다
            Map<String, Object> m = asMap(first);
            if (m != null) {
                String r = asString(m.get("routeName"));
                if (r == null) r = asString(m.get("name"));
                if (r == null) r = asString(m.get("number"));
                if (r == null) r = asString(m.get("lineName"));
                if (r != null && !r.isBlank()) return r;
            }

            // list 안 원소가 문자열이면 그대로 쓴다
            String s = asString(first);
            if (s != null && !s.isBlank()) return s;
        }

        //  route가 map일 수도 있고 문자열일 수도 있음
        Map<String, Object> routeObj = asMap(routeObjRaw);

        //  Map이면 routeName/name/number 순서로 찾기
        if (routeObj != null) {
            String r = asString(routeObj.get("routeName"));
            if (r == null) r = asString(routeObj.get("name"));
            if (r == null) r = asString(routeObj.get("number"));
            if (r != null) return r;
        }

        String routeStr = asString(routeObjRaw);
        if (routeStr != null && !routeStr.isBlank()) return routeStr;

        String line = asString(leg.get("line"));
        if (line != null && !line.isBlank()) return line;

        return null;
    }

    // 버스/지하철 노선명을 보기 좋게 정리
    private String normalizeTransitRouteLabel(String mode, String raw) {
        if (raw == null || raw.isBlank()) return null;
        String s = raw.trim();

        // 버스는 "번"이 없으면 붙여준다 (예: 416 -> 416번)
        if ("BUS".equalsIgnoreCase(mode)) {
            if (!s.contains("번")) s = s + "번";
            return s;
        }

        // 지하철은 "호선"이 없으면 붙여준다 (예: 7 -> 7호선)
        // 단, "분당선/신분당선"처럼 이미 "선"이 있으면 그대로 쓴다
        if ("SUBWAY".equalsIgnoreCase(mode)) {
            if (s.contains("선")) return s;
            if (!s.contains("호선")) s = s + "호선";
            return s;
        }

        return s;
    }

    private Map<String, Object> readJson(String raw) {
        try {
            return om.readValue(raw, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("JSON 파싱 실패", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object o) {
        if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asListOfMap(Object o) {
        if (!(o instanceof List<?> list)) return null;
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object v : list) {
            if (v instanceof Map<?, ?> m) out.add((Map<String, Object>) m);
        }
        return out;
    }

    private String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private Integer asInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }

    private Double asDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }
}