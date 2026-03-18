# 🤝 모인 (MOIN) : 신뢰 기반 크루 & 모임 플랫폼

  <h3 align="center">신뢰와 안정성을 핵심으로 설계한 모임 플랫폼</h3>

<p align="center">
모인은 사용자 간 신뢰 형성을 구조적으로 해결하고자 기획한 서비스입니다.  
활동 기록과 피드백을 기반으로 신뢰를 축적하고,  
안정적인 신청 처리와 실시간 소통 환경을 구현했습니다.
</p>

<p align="center">
단순 기능 구현을 넘어,  
데이터 정합성과 사용자 경험을 함께 고려한 백엔드 아키텍처를 설계했습니다.
</p>

---

## 👤 팀원

<table align="center">
  <tbody>
    <tr>
           <td align="center">
        <img src="https://github.com/user-attachments/assets/a6ca8520-27f4-40eb-9868-2cabdc482328" style="width:120px;height:120px;object-fit:cover;" alt="김도균"/><br />
        <sub><b><a href="https://github.com/kimdogyun">김도균</a></b></sub><br />
      </td>
      <td align="center">
        <img src="https://github.com/user-attachments/assets/bee05693-0606-4d7e-829e-d41ab69e65b1" style="width:120px;height:120px;object-fit:cover;" alt="박세민"/><br />
        <sub><b><a href="https://github.com/semin980520">박세민</a></b></sub><br />
      </td>
      <td align="center">
        <img src="https://github.com/user-attachments/assets/d4c3d32d-529b-41a9-9c42-26264a2e5bfc" style="width:120px;height:120px;object-fit:cover;" alt="이다은"/><br />
        <sub><b><a href="https://github.com/leeda973">이다은</a></b></sub><br />
      </td>
      <td align="center">
        <img src="https://github.com/user-attachments/assets/34c78d07-eece-4df8-a85b-b137cdd07080" style="width:120px;height:120px;object-fit:cover;" alt="최혜수"/><br />
        <sub><b><a href="https://github.com/Hyesu-Choi">최혜수</a></b></sub><br />
      </td>
    </tr>
  </tbody>
</table>

---

# 목차
1. [프로젝트 기획](#1-프로젝트-기획)
2. [주요 기능 및 기술](#2-주요-기능-및-기술)
3. [Tech Stack](#3-tech-stack)
4. [시스템 아키텍처](#4-시스템-아키텍처)
5. [성능 개선 및 트러블 슈팅](#5-성능-개선-및-트러블-슈팅)
6. [프로젝트 시연](#6-프로젝트-시연)
7. [그 외 산출물](#7-그-외-산출물)

---

# 1. 프로젝트 기획

<details>
<summary><strong>🔍 기획 배경</strong></summary>
<br>

기존 모임 플랫폼은 사람을 연결하는 기능에는 충실했지만, 신뢰 형성과 운영 측면에서 해결되지 않은 문제들이 존재했습니다.

- 참여자의 신뢰도를 판단할 객관적 기준 부재
- 노쇼 및 비매너 유저 관리의 어려움
- 모임 운영 및 정산 과정의 불투명성
- 실시간 소통 부족으로 인한 참여 혼선

모인은 이러한 문제를 해결하기 위해  **데이터 기반 신뢰 시스템과 실시간 소통 구조를 갖춘 모임 플랫폼**을 기획했습니다.

</details>

<details>
<summary><strong>💡 해결 방향</strong></summary>
<br>

> "사람 중심의 모임 문화를, 데이터와 기술로 설계하다."

| | 문제 | 해결 방향 |
|:---:|:---|:---|
| 1️⃣ | 참여자 신뢰도 판단 기준 부재 | 활동 이력 기반 매너 점수 시스템 도입 |
| 2️⃣ | 실시간 소통 부족으로 인한 참여 혼선 | WebSocket + SSE 기반 실시간 채팅 및 알림 구현 |
| 3️⃣ | 정산/환불 과정의 불투명성 | 스케줄러 기반 자동 정산 및 상태 기반 환불 처리 |
| 4️⃣ | 모임 참여 경험의 단절 | 피드 기반 커뮤니티 확장으로 지속적 연결 유도 |

</details>

---

# 2. 주요 기능 및 기술

<details>
<summary><strong>📋 주요 기능 상세 보기</strong></summary>
<br>

<details>
<summary><strong>🔒 인증 및 사용자 관리</strong></summary>
<br>

OAuth2 소셜 로그인과 JWT 기반 인증을 적용하여 서버 확장성을 고려한 Stateless 인증 아키텍처 구현
- OAuth2 소셜 로그인 구현
- JWT AccessToken / RefreshToken 발급
- RefreshToken Redis 저장 및 재발급 로직 구현
- Spring Security 기반 인증 필터 구성

![Google 로그인](https://github.com/user-attachments/assets/1c79a502-e931-4b0e-8bc3-9165f63e1fb3)
![Kakao 로그인](https://github.com/user-attachments/assets/239dc0fd-1c14-4357-8d90-b44df5b538a0)

</details>


<details>
<summary><strong>🤝 신뢰 지표 및 커뮤니티 시스템</strong></summary>
<br>

사용자의 활동 데이터를 기반으로 행동 중심 데이터 모델링 적용
- 모임 참여, 리뷰, 신고 이력 기반 매너 점수 산정
- 점수 변동 이력 별도 관리
- 크루 생성 및 역할 기반 권한 관리
- 활동 피드 작성, 댓글, 좋아요 기능
- 이미지 파일은 AWS S3에 저장

![매너 평가](https://github.com/user-attachments/assets/18f271eb-d9e6-4493-af0a-009e17ae5f12)
![출석 체크, 모임 종료, 매너 평가](https://github.com/user-attachments/assets/a8bf7b77-48d0-47da-95f1-67f90eb9185e)
![가입 요청 승인/거절](https://github.com/user-attachments/assets/52dafcff-32a2-46c9-b996-92d9d4fdf2c2)
![피드 댓글 및 좋아요](https://github.com/user-attachments/assets/a2d6ed06-7259-4da1-959f-b0be0e75db6f)

</details>


<details>
<summary><strong>👥 크루 및 모임 관리</strong></summary>
<br>

크루와 모임 도메인을 중심으로 생성, 참여, 운영 흐름을 관리하도록 설계
- 크루 가입 신청, 승인, 거절, 탈퇴 기능 구현
- 모임 신청 및 참여 관리
- Redis를 활용한 크루 찜하기 기능 구현
- 카카오 지도 기반 모임 장소 표시 및 좌표 기반 위치 정보 처리
- Tmap API를 활용한 현재 위치 기준 경로 탐색 기능 구현(대중교통, 차량, 도보)
- 크루원 및 모임원의 가입 상태 관리

![가입 요청 승인/거절](https://github.com/user-attachments/assets/52dafcff-32a2-46c9-b996-92d9d4fdf2c2)
![무료모임 참여](https://github.com/user-attachments/assets/343732ca-8b84-43e2-ad19-8ff0103ed0e5)
![유료 모임 참여](https://github.com/user-attachments/assets/90130455-e523-4240-bc66-54e7b02ef312)
![찜 기능](https://github.com/user-attachments/assets/64ff0add-d723-4c69-898e-21ef40106f31)
![길찾기 및 카카오맵 연동](https://github.com/user-attachments/assets/19f9c99e-e4ca-4b7c-90fb-918e19b68fa0)
![크루원 목록 및 운영진 목록 관리](https://github.com/user-attachments/assets/308da1ad-d8f2-4610-b3d9-c6c72b557251)

</details>


<details>
<summary><strong>💬 실시간 메시징 시스템</strong></summary>
<br>

이벤트 기반 메시징 구조를 적용해 서버 확장 환경에서도 안정적으로 동작하도록 실시간 아키텍처 구현
- WebSocket + STOMP 기반 채팅
- Redis Pub/Sub을 통한 서버 간 메시지 동기화
- 채팅 메시지 비동기 저장
- SSE 기반 실시간 알림
- 주요 이벤트 발생 시 알림 전파 구조 구현

![크루 단체 채팅방 및 DM](https://github.com/user-attachments/assets/66eded38-5d31-4388-9485-f7a8b31cdfa1)
![사진 전송, 공지 메시지 및 수정/삭제](https://github.com/user-attachments/assets/7f299b74-db03-42cd-8ff7-0160bfc18b33)
![알림 페이지](https://github.com/user-attachments/assets/b21c99ce-75dc-4e83-b66f-a1d3f5f17eb9)

</details>

</details>

<details>
<summary><strong>💡 핵심 기술</strong></summary>

### 🔐 인증 / 보안

| 기술 | 도입 이유 | 상세 설명 |
| :--- | :--- | :--- |
| **OAuth2 소셜 로그인** | 사용자 편의성 향상 | 이메일 로그인 외에 구글, 카카오 소셜 로그인을 도입하여 가입 장벽을 낮췄습니다. |
| **JWT AccessToken / RefreshToken** | Stateless 인증 아키텍처 | 서버 확장성을 고려해 세션 대신 JWT 기반 인증을 적용했습니다. |
| **Redis 토큰 / 인증키 관리** | 빠른 검증 + 자동 만료 처리 | Refresh Token과 인증 코드를 Redis에 저장하고 TTL로 자동 만료 처리하여 보안성과 성능을 동시에 확보했습니다. |
| **논리적 삭제 (Soft Delete)** | 결제/환불 데이터 보존 | 탈퇴·삭제 시 상태값(delYN)만 변경하여 연관 데이터의 연쇄 삭제를 방지하고 데이터 무결성을 유지했습니다. |

---

### 💬 실시간 통신

| 기술 | 도입 이유 | 상세 설명 |
| :--- | :--- | :--- |
| **WebSocket + STOMP 기반 실시간 채팅** | 양방향 지속 연결 필요 | STOMP 프로토콜로 메시지 발행/구독 구조를 체계적으로 관리하고, 채팅 메시지는 비동기로 저장하여 서버 부하를 최소화했습니다. |
| **SSE 기반 실시간 알림** | 단방향 서버 푸시 | 크루/모임 가입, 정산, 환불 이벤트 발생 시 사용자에게 실시간으로 알림을 전달합니다. |
| **Redis Pub/Sub** | 멀티 서버 메시지 동기화 | 서버 간 채팅 및 알림 메시지를 Pub/Sub 구조로 동기화하여 다중 서버 환경에서도 안정적인 실시간 통신을 보장합니다. |

---

### ⚡ 성능 최적화

| 기술 | 도입 이유 | 상세 설명 |
| :--- | :--- | :--- |
| **Redis 캐싱 처리** | DB 조회 부하 감소 | 자주 조회되는 데이터에 Redis 캐시를 적용하고, TTL 및 캐시 무효화 처리로 일관성을 유지했습니다. |
| **Redis 크루 찜하기** | 빠른 상태 처리 | 찜하기 상태를 Redis로 관리하여 빠른 응답성과 사용자 반응성을 확보했습니다. |
| **S3 Presigned URL 이미지 업로드** | 백엔드 서버 부하 분산 | 클라이언트가 서버를 거치지 않고 S3에 직접 업로드하도록 구현하여 이미지 처리 부하를 제거했습니다. |

---

### 💳 결제 / 정산

| 기술 | 도입 이유 | 상세 설명 |
| :--- | :--- | :--- |
| **포트원(카카오페이) API 결제** | 간편하고 안전한 결제 환경 | 포트원 API를 연동하여 사용자가 카카오페이로 편리하게 결제할 수 있도록 했습니다. |
| **스케줄러 기반 자동 정산 및 환불** | 운영 투명성 확보 | Spring Scheduler로 모임 시작 3시간 전 자동 정산을 처리하고, 정산 완료 후에는 환불이 차단되도록 상태 롤백 로직을 구현했습니다. |
| **동시성 제어** | 정원 초과 방지 | 동시 신청 상황에서 정원 무결성을 보장하기 위한 동시성 제어를 적용했습니다. |

---

### 🗺️ 위치 / 인프라

| 기술 | 도입 이유 | 상세 설명 |
| :--- | :--- | :--- |
| **Kakao Map API** | 직관적인 위치 시각화 | 모임 장소와 경로를 지도에 표시하고 카카오맵 길찾기와 연계했습니다. |
| **Tmap API 경로 탐색** | 다양한 이동수단 지원 | 주소를 좌표로 변환하고 현재 위치 기준 대중교통·차량·도보 경로를 탐색할 수 있도록 구현했습니다. |

---

### 🚀 배포 / 인프라

| 기술 | 도입 이유 | 상세 설명 |
| :--- | :--- | :--- |
| **AWS S3 + CloudFront** | 정적 파일 글로벌 배포 | Vue.js 빌드 결과물을 S3에 호스팅하고 CloudFront CDN을 통해 빠른 응답속도와 HTTPS를 제공합니다. |
| **Docker 멀티스테이지 빌드** | 이미지 경량화 | eclipse-temurin 기반으로 빌드 스테이지와 실행 스테이지를 분리하여 최종 이미지 크기를 최소화했습니다. |
| **GitHub Actions CI/CD** | 자동화 배포 파이프라인 | main 브랜치 push 시 빌드 → ECR push → kubectl rollout restart 까지 자동 수행하여 무중단 롤링 업데이트를 실현했습니다. |
| **AWS EKS** | 고가용성 및 자동 복구 | Spring Boot 서버를 EKS로 오케스트레이션하여 파드 장애 시 자동 재시작, replica 2개로 무중단 서비스를 구현했습니다. |
| **AWS ECR** | 컨테이너 이미지 관리 | Docker 이미지를 ECR 프라이빗 레지스트리에 저장하고 EKS가 최신 이미지를 Pull하여 배포합니다. |
| **HPA (Horizontal Pod Autoscaler)** | 트래픽 기반 자동 스케일링 | CPU 사용률 50% 기준으로 파드를 최소 1개에서 최대 3개까지 자동으로 수평 확장합니다. |
| **Nginx Ingress + AWS Load Balancer** | 외부 트래픽 라우팅 | AWS LB → Ingress Controller → Service 순으로 트래픽을 라우팅하여 도메인 기반 접근을 구현했습니다. |

</details>

---

# 3. Tech Stack

## ⚙️ Backend
<p>
  <img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=hibernate&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring_Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white"/>
  <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white"/>
  <img src="https://img.shields.io/badge/WebSocket-000000?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/STOMP-6DB33F?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Lombok-FF0000?style=for-the-badge&logo=java&logoColor=white"/>
  <img src="https://img.shields.io/badge/Redis-FF4438?style=for-the-badge&logo=redis&logoColor=white"/>
  <img src="https://img.shields.io/badge/SSEEmitter-000000?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white"/>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
</p>

## 🖥️ Frontend
<p>
  <img src="https://img.shields.io/badge/Vue.js-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white"/>
  <img src="https://img.shields.io/badge/Vue_Router-4FC08D?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Vuetify-1867C0?style=for-the-badge&logo=vuetify&logoColor=white"/>
  <img src="https://img.shields.io/badge/Axios-5A29E4?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white"/>
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black"/>
  <img src="https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white"/>
</p>

## 🗄️ Database
<p>
  <img src="https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white"/>
  <img src="https://img.shields.io/badge/Redis-FF4438?style=for-the-badge&logo=redis&logoColor=white"/>
  <img src="https://img.shields.io/badge/ERDCloud-1F1F1F?style=for-the-badge&logo=linkerd&logoColor=white"/>
  <img src="https://img.shields.io/badge/MySQL_Workbench-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/>
  <img src="https://img.shields.io/badge/SQL-336791?style=for-the-badge&logo=postgresql&logoColor=white"/>
</p>

## 🚀 Infra & DevOps
<p>
  <img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white"/>
  <img src="https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white"/>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
  <img src="https://img.shields.io/badge/AWS_EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white"/>
  <img src="https://img.shields.io/badge/AWS_RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white"/>
  <img src="https://img.shields.io/badge/AWS_S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white"/>
  <img src="https://img.shields.io/badge/AWS_CloudFront-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white"/>
</p>

## 🤝 Collaboration & Tools
<p>
  <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white"/>
  <img src="https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white"/>
  <img src="https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white"/>
  <img src="https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white"/>
  <img src="https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white"/>
  <img src="https://img.shields.io/badge/IntelliJ_IDEA-000000?style=for-the-badge&logo=intellijidea&logoColor=white"/>
  <img src="https://img.shields.io/badge/VS_Code-007ACC?style=for-the-badge&logo=visualstudiocode&logoColor=white"/>
  <img src="https://img.shields.io/badge/ERDCloud-1F1F1F?style=for-the-badge&logo=linkerd&logoColor=white"/>
</p>

---

# 4. 시스템 아키텍처

<details>
  <summary><b>시스템 아키텍처</b></summary>

   <img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/27817c5c-76f7-41d0-b738-74d423810f0f" />

</details>

---

# 5. 성능 개선 및 트러블 슈팅
<details>
  <summary><b>찜하기 동시성 테스트</b></summary>

### 찜하기 동시성 테스트
동일 사용자와 동일 크루를 대상으로 찜하기 요청 100건을 동시에 전송한 결과,  
일부 요청에서 락 경합이 발생해 요청이 실패했습니다.  
중복 찜은 방지할 수 있었지만, 동시 요청 상황에서는 안정적으로 처리되지 않는 문제가 있었습니다.
<p align="center">
  <img width="1000" alt="찜하기 동시성 테스트 실패 Response Data" src="https://github.com/user-attachments/assets/582d2934-3dd3-450e-9dce-c309bafcf453" />
</p>
<p align="center">
  <img width="1000" alt="찜하기 동시성 테스트 실패 Summary Report" src="https://github.com/user-attachments/assets/32755d79-0cf9-4a02-afef-6bf256ef128e" />
</p>

### 찜하기 동시성 개선
동일한 사용자-크루 조합에 대한 동시 찜 요청에서  
중복 저장과 찜 수 정합성 문제가 발생하지 않도록 로직을 보완했습니다.  
이후 동일 조건으로 100건의 동시 요청을 다시 테스트한 결과,  
중복 데이터 없이 1건만 저장되었고 찜 수도 1회만 증가했으며,  
전체 요청이 오류 없이 안정적으로 처리되는 것을 확인했습니다.

- 중복 저장 방지: `(crew_id, user_id)` 유니크 제약조건 활용
- count 정합성 보장: 실제 insert 성공 시에만 찜 수 증가
- 동시 요청 안정화: 경쟁 상황에서 발생하는 일부 예외가 전체 요청 실패로 이어지지 않도록 보완
<p align="center">
  <img width="1000" alt="찜하기 동시성 개선 Summary Report" src="https://github.com/user-attachments/assets/56f51e0e-e952-4dcd-a0ae-79721e2d4867" />
</p>
<p align="center">
  <img width="1000" alt="찜하기 동시성 개선 응답 결과" src="https://github.com/user-attachments/assets/98408029-67c9-4d20-8391-cc47e6ac429b" />
</p>
<p align="center">
  <img width="400" alt="찜하기 동시성 개선 DB 검증 결과" src="https://github.com/user-attachments/assets/6eba178e-e7ed-4f59-8be7-25c011a3cceb" />
</p>

### 찜취소 동시성 테스트
동일 사용자와 동일 크루를 대상으로 찜취소 요청 100건을 동시에 전송한 결과,  
모든 요청이 정상 처리되었습니다.  
실제로 삭제가 발생한 경우에만 찜 수가 감소하도록 구성하여,  
중복 차감이나 음수 처리 없이 안정적으로 처리됨을 확인했습니다.
<p align="center">
  <img width="1000" alt="찜취소 동시성 테스트 Summary Report" src="https://github.com/user-attachments/assets/cebdde3d-4a2c-4ae9-a39f-1f53811bc661" />
</p>
<p align="center">
  <img width="1000" alt="찜취소 동시성 테스트 응답 결과" src="https://github.com/user-attachments/assets/052fcce7-5718-42b2-9544-ea0f43a62b4b" />
</p>
<p align="center">
  <img width="400" alt="찜취소 동시성 테스트 DB 검증 결과" src="https://github.com/user-attachments/assets/a35c04bb-9f05-4d3e-be42-8e1f094ef4b1" />
</p>
</details>

<details>
  <summary><b>피드,댓글 좋아요 동시성 테스트</b></summary>

### 피드·댓글 좋아요 동시성 테스트
동일 사용자와 동일 피드, 동일 사용자와 동일 댓글에 대해 좋아요 및 좋아요 취소 요청 100건을 동시에 전송해 테스트했습니다.  
그 결과 모든 요청이 오류 없이 정상 처리되었습니다.  
또한 데이터베이스 조회 결과도 기대값과 일치하여,  
피드 좋아요와 댓글 좋아요 모두 동시성 상황에서도 안정적으로 처리됨을 확인했습니다.

#### 피드 좋아요
<p align="center">
  <img width="1000" alt="피드 좋아요 동시성 테스트 Summary Report" src="https://github.com/user-attachments/assets/4358f471-470d-4109-aa11-ec9b09d86a2f" />
</p>
<p align="center">
  <img width="1000" alt="피드 좋아요 동시성 테스트 응답 결과" src="https://github.com/user-attachments/assets/9e05415f-9864-472f-a053-6f5dbe4ae0f2" />
</p>
<p align="center">
  <img width="400" alt="피드 좋아요 동시성 테스트 DB 검증 결과" src="https://github.com/user-attachments/assets/032e9b77-b98e-4b05-b22f-0f8c3e3cbd76" />
</p>

#### 피드 좋아요 취소
<p align="center">
  <img width="1000" alt="피드 좋아요 취소 동시성 테스트 Summary Report" src="https://github.com/user-attachments/assets/19e295b4-3812-4a40-aacf-cdf42d8058f7" />
</p>
<p align="center">
  <img width="1000" alt="피드 좋아요 취소 동시성 테스트 응답 결과" src="https://github.com/user-attachments/assets/5dbbf7cc-c7f5-48ca-b20d-3b1a17f1d356" />
</p>
<p align="center">
  <img width="400" alt="피드 좋아요 취소 동시성 테스트 DB 검증 결과" src="https://github.com/user-attachments/assets/0dead06a-8580-47c8-b480-69dc305a0901" />
</p>

#### 댓글 좋아요
<p align="center">
  <img width="1000" alt="댓글 좋아요 동시성 테스트 Summary Report" src="https://github.com/user-attachments/assets/685e0b26-bc47-4e25-87a9-e9cb2855af98" />
</p>
<p align="center">
  <img width="1000" alt="댓글 좋아요 동시성 테스트 응답 결과" src="https://github.com/user-attachments/assets/3f0b18e6-17b5-4035-b169-d46f746c6e62" />
</p>
<p align="center">
  <img width="400" alt="댓글 좋아요 동시성 테스트 DB 검증 결과" src="https://github.com/user-attachments/assets/0ea37bc3-1815-4c47-86af-f101b5f42f36" />
</p>

#### 댓글 좋아요 취소
<p align="center">
  <img width="1000" alt="댓글 좋아요 취소 동시성 테스트 Summary Report" src="https://github.com/user-attachments/assets/7bbea7f3-8ee6-4181-aaa2-186f39e1994e" />
</p>
<p align="center">
  <img width="1000" alt="댓글 좋아요 취소 동시성 테스트 응답 결과" src="https://github.com/user-attachments/assets/dc6ebaf5-a545-4b78-81de-ab3d1d38b96e" />
</p>
<p align="center">
  <img width="400" alt="댓글 좋아요 취소 동시성 테스트 DB 검증 결과" src="https://github.com/user-attachments/assets/9f61632b-4415-45b0-8883-a9f25518ab32" />
</p>
</details>

<details>
  <summary><b>크루 가입 신청 좋아요 동시성 테스트</b></summary>

### 크루 가입 신청 동시성 테스트
서로 다른 사용자 100명이 로그인한 뒤 동일한 크루에 동시에 가입 신청 요청을 전송해 동시성 테스트를 진행했습니다.  
그 결과 모든 요청이 정상 처리되었으며,  
동일 시점에 요청이 집중되는 상황에서도 각 사용자의 가입 신청이 누락되거나 중복 저장되지 않았습니다.  

이후 데이터베이스를 검증한 결과,  
가입 신청 전 0명이었던 신청 인원이 테스트 이후 100명으로 증가해 기대값과 정확히 일치하는 것을 확인했습니다.  
이를 통해 크루 가입 신청 기능이 대량의 동시 요청 환경에서도 안정적으로 동작함을 검증했습니다.

<p align="center">
  <img width="1000" alt="크루 가입 신청 동시성 테스트 Summary Report" src="https://github.com/user-attachments/assets/a65f7f2a-0a83-47d9-9459-afaa5a806906" />
</p>
<p align="center">
  <img width="1000" alt="크루 가입 신청 동시성 테스트 응답 결과" src="https://github.com/user-attachments/assets/09678312-2c8c-4a0a-85fc-867aeff739c0" />
</p>
<p align="center">
  <img width="400" alt="크루 가입 신청 전 가입 신청자 수" src="https://github.com/user-attachments/assets/6cff9fb2-21e2-4909-881a-9cf5106e47bb" />
</p>
<p align="center">
  <img width="400" alt="크루 가입 신청 후 가입 신청자 수" src="https://github.com/user-attachments/assets/90d315fd-18a8-4186-b769-7dbcbbf21e08" />
</p>

</details>

<details>
  <summary><b>Redis 캐싱 처리</b></summary>

### 캐싱 적용 전 - SELECT 쿼리 발생 확인

크루 목록 조회는 사용자가 메인 화면에 진입할 때마다 호출되는 빈도가 높은 API입니다.  
캐싱 적용 전에는 매 요청마다 DB에 SELECT 쿼리가 발생하여,  
동일한 데이터임에도 불구하고 불필요한 DB I/O가 반복적으로 누적되는 문제가 있었습니다.

<p align="center">
  <img width="1064" height="335" alt="캐싱 전 SELECT 쿼리 발생" src="https://github.com/user-attachments/assets/c4b83268-9a09-45e4-b523-d33c25a3ed06" />
</p>

### Redis 캐싱 적용

<img width="1190" height="289" alt="Image" src="https://github.com/user-attachments/assets/6021a5da-0cc7-434a-a3fe-9cd0cbf07541" />

조회 빈도가 높고 실시간성이 크게 요구되지 않는 크루 목록에 `@Cacheable` 어노테이션을 적용하여  
첫 요청 시에만 DB에서 데이터를 조회하고, 이후 동일한 요청은 Redis 캐시에서 즉시 응답하도록 구현했습니다.  
TTL을 설정해 일정 시간 이후 캐시가 자동 만료되도록 하고,  
크루 정보 생성·수정·삭제 시 `@CacheEvict`로 캐시를 즉시 무효화하여 데이터 정합성을 유지했습니다.

<p align="center">
  <img width="1106" height="900" alt="Redis 캐싱 처리 확인" src="https://github.com/user-attachments/assets/032cc79d-ddc9-4e66-b1e0-2621a9c886d4" />
</p>

### 개선 결과

캐싱 적용 이후 동일 조건의 크루 목록 요청에 대해 DB SELECT 쿼리가 발생하지 않고  
Redis에서 즉시 응답이 반환되는 것을 확인했습니다.  
반복 조회 시 발생하던 불필요한 쿼리를 제거하여 DB 부하를 줄이고,  
응답 처리 흐름을 단순화했습니다.

</details>

---

# 6. 프로젝트 시연

<details>
  <summary><b>1. 사용자 관리 (User)</b></summary>

 <details>
  <summary><b>1-1. 회원가입</b></summary>

  ![회원가입1](https://github.com/user-attachments/assets/f1505b35-45e8-420f-afd4-cbb587181256)

  ![회원가입2](https://github.com/user-attachments/assets/045a571e-674b-4060-a290-958c57c0e89d)

</details>

  <details>
  <summary><b>1-2. 이메일/비밀번호 로그인</b></summary>

  ![로그인](https://github.com/user-attachments/assets/1f5b5eb5-9fef-4ab4-961a-25d8fa42e94f)

</details>

<details>
  <summary><b>1-3. Google, Kakao 소셜 로그인 지원</b></summary>

  <h4>Google 로그인</h4>

  ![구글 로그인](https://github.com/user-attachments/assets/1c79a502-e931-4b0e-8bc3-9165f63e1fb3)

  <h4>Kakao 로그인</h4>

  ![카카오 로그인](https://github.com/user-attachments/assets/239dc0fd-1c14-4357-8d90-b44df5b538a0)

</details>

  <details>
  <summary><b>1-4. 비밀번호 재설정 페이지 제공</b></summary>

  ![비밀번호 재설정](https://github.com/user-attachments/assets/5c7102b0-5c14-4559-b9e1-a8cb50782b21)

</details>

</details>


<details>
  <summary><b>2. 마이페이지</b></summary>

<details>
  <summary><b>2-1. 마이페이지 화면</b></summary>

  ![마이페이지](https://github.com/user-attachments/assets/58abf350-b7a7-4112-999e-242a74e712a9)

</details>

  <details>
  <summary><b>2-2. 프로필 수정</b></summary>

  ![프로필수정](https://github.com/user-attachments/assets/79e93ef9-5e5a-4d29-8f62-22f1deca392f)

</details>

<details>
  <summary><b>2-3. 내 모임 일정 확인</b></summary>

  ![내모임일정](https://github.com/user-attachments/assets/97a0ed8b-0b90-49c8-bc8f-76fc454e22ab)

</details>
</details>


<details>
  <summary><b>3. 회비 결제</b></summary>

  ![회비 결제](https://github.com/user-attachments/assets/4a6dc8b6-3bf5-4aa9-8a2e-fc2938ac1831)

</details>


<details>
  <summary><b>4. 크루 메인 및 조회 (Crew Management)</b></summary>

  <details>
  <summary><b>4-1. 메인 화면</b></summary>

  ![메인 화면](https://github.com/user-attachments/assets/5ec8835a-c510-4a44-8f27-ff603c3df851)

</details>

  <details>
  <summary><b>4-2. 카테고리별 크루 탐색</b></summary>

  ![카테고리별 크루 탐색](https://github.com/user-attachments/assets/4778c69e-fe67-42d2-a343-0740257a2a86)

</details>

  <details>
  <summary><b>4-3. 지역/구 단위 필터링</b></summary>

  ![지역/구 단위 필터링](https://github.com/user-attachments/assets/7b46a74b-9508-4a99-a23e-14e76b6cdf43)

</details>

  <details>
  <summary><b>4-4. 검색 및 자동완성 검색어 제공</b></summary>

  ![검색 및 자동완성 검색어 제공](https://github.com/user-attachments/assets/b30b036b-e21e-4d57-a81f-0834d39868fb)

</details>

  <details>
  <summary><b>4-5. 최근 본 크루 기록 제공</b></summary>

  ![최근 본 크루 기록 제공](https://github.com/user-attachments/assets/66f6339a-5d22-44a1-92b7-e707f1812b0d)

</details>

 <details>
  <summary><b>4-6. 찜(즐겨찾기) 기능 지원</b></summary>

  ![찜 기능 지원](https://github.com/user-attachments/assets/64ff0add-d723-4c69-898e-21ef40106f31)

</details>

</details>


<details>
  <summary><b>5. 크루 활동</b></summary>

  <details>
  <summary><b>5-1. 크루 가입 신청</b></summary>

  ![크루 가입 신청](https://github.com/user-attachments/assets/5077f9ff-c3ab-4b44-9d36-6e4ef6e92073)

</details>

 <details>
  <summary><b>5-2. 가입 요청 승인/거절 (운영진)</b></summary>

  ![가입 요청 승인/거절](https://github.com/user-attachments/assets/52dafcff-32a2-46c9-b996-92d9d4fdf2c2)

</details>

<details>
  <summary><b>5-3. 크루원 목록 및 운영진 목록 관리 (운영진)</b></summary>

  ![크루원 목록 및 운영진 목록 관리](https://github.com/user-attachments/assets/308da1ad-d8f2-4610-b3d9-c6c72b557251)

</details>

  <details>
  <summary><b>5-4. 크루 정보 수정 및 삭제</b></summary>

  <h4>크루 정보 수정</h4>

  ![크루 정보 수정](https://github.com/user-attachments/assets/02953568-e0e4-40f3-8cfa-d6f6536107c8)

  <h4>크루 삭제</h4>

  ![크루 삭제](https://github.com/user-attachments/assets/1b1c4a59-6256-459d-ac39-b6cf19527fd5)

</details>

<details>
  <summary><b>5-5. 크루별 피드 (게시판)</b></summary>

  ![크루별 피드](https://github.com/user-attachments/assets/44b25bee-411e-4cde-811a-3baccdce0e69)

</details>

<details>
  <summary><b>5-6. 피드 생성/수정/삭제</b></summary>

  <h4>피드 생성</h4>

  ![피드 생성](https://github.com/user-attachments/assets/21b00180-4a9e-487d-994b-ebf8a24b130c)

  <h4>피드 수정</h4>

  ![피드 수정](https://github.com/user-attachments/assets/2d1a0f3f-83c8-4bc2-ad2f-e6e9fcee4533)

  <h4>피드 삭제</h4>

  ![피드 삭제](https://github.com/user-attachments/assets/36df0676-6360-4731-934b-e59c4036dc9c)

</details>

  <details>
  <summary><b>5-7. 피드 댓글 및 좋아요</b></summary>

  ![피드 댓글 및 좋아요](https://github.com/user-attachments/assets/a2d6ed06-7259-4da1-959f-b0be0e75db6f)

</details>

</details>


<details>
  <summary><b>6. 크루 내 모임 활동</b></summary>

  <details>
  <summary><b>6-1. 크루별 모임 목록 조회</b></summary>

  ![모임리스트](https://github.com/user-attachments/assets/946d35ca-4394-41c0-af8b-cb92fd23d89e)

</details>

<details>
  <summary><b>6-2. 개별 모임 참여/나가기 기능</b></summary>

  <h4>무료모임 참여</h4>

  ![무료모임참여](https://github.com/user-attachments/assets/343732ca-8b84-43e2-ad19-8ff0103ed0e5)

  <h4>무료모임 나가기</h4>

  ![무료모임나가기](https://github.com/user-attachments/assets/951a1c27-417a-4c83-89f8-06a66d1aca3c)

  <h4>유료모임 참여</h4>

  ![유료모임참여](https://github.com/user-attachments/assets/90130455-e523-4240-bc66-54e7b02ef312)

  <h4>유료모임 나가기</h4>

  ![유료모임나가기](https://github.com/user-attachments/assets/64dfc399-a304-4d7d-9176-e4023d97df15)

</details>

 <details>
  <summary><b>6-3. 길찾기 및 카카오맵 연동</b></summary>

  ![길찾기및카카오맵연동](https://github.com/user-attachments/assets/19f9c99e-e4ca-4b7c-90fb-918e19b68fa0)

</details>

  <details>
  <summary><b>6-4. 모집 상태 변경 (운영자)</b></summary>

  ![모집상태변경](https://github.com/user-attachments/assets/6caee158-f9c9-4c31-b8f6-4549b4f831d8)

</details>

  <details>
  <summary><b>6-5. 출석 체크, 모임 종료, 매너 평가 기능 제공</b></summary>

  <h4>출석 체크 / 모임 종료</h4>

  ![출석체크_모임종료](https://github.com/user-attachments/assets/a8bf7b77-48d0-47da-95f1-67f90eb9185e)

  <h4>매너 평가</h4>

  ![매너평가](https://github.com/user-attachments/assets/18f271eb-d9e6-4493-af0a-009e17ae5f12)

</details>

</details>


<details>
  <summary><b>7. 채팅</b></summary>

 <details>
  <summary><b>7-1. 크루 단체 채팅방 및 멤버들과의 DM 기능</b></summary>

![Image](https://github.com/user-attachments/assets/66eded38-5d31-4388-9485-f7a8b31cdfa1)

</details>

<details>
  <summary><b>7-2. 사진 전송, 공지 메시지 및 수정/삭제 지원</b></summary>

  ![Image](https://github.com/user-attachments/assets/7f299b74-db03-42cd-8ff7-0160bfc18b33)

</details>

</details>


<details>
  <summary><b>8. 시스템 알림</b></summary>

  <details>
  <summary><b>8-1. 알림 페이지 제공</b></summary>

  ![알림페이지](https://github.com/user-attachments/assets/b21c99ce-75dc-4e83-b66f-a1d3f5f17eb9)

</details>

  <details>
  <summary><b>8-2. 읽지 않은 알림 개수 전역 관리</b></summary>

  ![읽지않은알림개수전역관리](https://github.com/user-attachments/assets/8f6ed531-ac27-40b7-aab4-45fd8a216a8f)

</details>

</details>

---

# 7. 그 외 산출물
<details>
  <summary><b>
    <a href='https://docs.google.com/spreadsheets/d/1GyJp3dJQPYV7_bJa6nXNSuxTnrFpBSRpovNYE-cfyMI/edit?usp=sharing' style="text-decoration: none; color: inherit;"> 
      요구사항 명세서
    </a>
  </b></summary>
  <p align="left">
    <img width="418" height="1120" alt="요구사항 명세서" src="https://github.com/user-attachments/assets/d5f2bd67-4a19-47e0-b668-67c3779c5a60" />
  </p>
</details>

<details>
  <summary><b>
    <a href='https://docs.google.com/spreadsheets/d/1hEHrLjJf3iCyrdZ7XBmxmMM2BLnLyQgs8BqKCxm97bk/edit?usp=sharing' style="text-decoration: none; color: inherit;"> 
      기능 명세서
    </a>
  </b></summary>
  <p align="left">
    <img width="665" height="890" alt="기능 명세서" src="https://github.com/user-attachments/assets/19610d99-3384-44a1-9554-6405dc4e7906" />
  </p>
</details>

<details>
  <summary><b> <a href='https://www.erdcloud.com/d/zAo9bewkcJp3R8Zgy' style="text-decoration: none; color: inherit;"> ERD</a></b></summary>
  <img width="1469" height="800" alt="Image" src="https://github.com/user-attachments/assets/1753bd74-cfa5-475f-81d9-a36a303fa44c" />
</details>

<details>
  <summary><b> <a href='https://docs.google.com/spreadsheets/d/1PzEMXll7Vz4iZjhTJeZoCeLAkSu9PK_6pxyyyXC5JYs/edit?usp=sharing' style="text-decoration: none; color: inherit;"> WBS</a></b></summary>
  <p align="left">
    <img width="614" height="1111" alt="WBS 1" src="https://github.com/user-attachments/assets/bb283b5e-6bfb-4808-b3c9-cfe301de4613" />
    <img width="613" height="437" alt="WBS 2" src="https://github.com/user-attachments/assets/e87d8a64-7861-4b44-bfb3-390cc875c9d8" />
  </p>
</details>

* <span> <b><a href='https://documenter.getpostman.com/view/51059789/2sBXcHhJYf'>api 명세서</a></b></span>
&nbsp;&nbsp;
<span> <br/>
* <b><a href='https://www.figma.com/design/yDzBMgiUFMXo8bjLPz25Gh/team4_moin?node-id=0-1&t=nxcIpNQ7qQH028Mg-1'>피그마</a></b></span>
