# 🤝 모인 (MOIN) : 신뢰 기반 크루 & 모임 플랫폼

<!-- <p align="center">
  <img src="Images/브랜드 이미지.png" alt="[로고]" width="400"/>
</p> -->

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
        <img src="https://github.com/user-attachments/assets/d8f6c8a6-d9cc-47ba-92d6-beece2c8c702" style="width:120px;height:120px;object-fit:cover;" alt=""/><br /><sub><b> 김도균 </b></sub><br /></td>
      <td align="center"><img src="https://github.com/user-attachments/assets/8558d775-cb9c-41a1-9154-425590b020f9" style="width:120px;height:120px;object-fit:cover;" alt=""/><br /><sub><b> 박세민 </b></sub><br /></td>
      <td align="center"><img src="https://github.com/user-attachments/assets/84020147-49fa-4e67-a71a-acdd68e4391d" style="width:120px;height:120px;object-fit:cover;" alt=""/><br /><sub><b> 이다은 </b></sub><br /></td>
      <td align="center"><img src="https://github.com/user-attachments/assets/5f5dd49b-961b-47cb-8e55-ab49f65d5435" style="width:120px;height:120px;object-fit:cover;" alt=""/><br /><sub><b> 최혜수 </b></sub><br /></td>
    </tr>
  </tbody>
</table>

</div>

---

# 목차
1. [프로젝트 기획](#1-프로젝트-기획)
2. [주요 기능 및 기술](#2-주요-기능-및-기술)
3. [산출물](#3-산출물)
4. [Tech Stack](#4-tech-stack)
5. [프로젝트 시연](#5-프로젝트-시연)
   

---

# 1. 프로젝트 기획

## 🔍 기획 배경
기존 모임 플랫폼은 사람을 연결하는 기능에는 충실했지만,  
신뢰 형성과 운영 측면에서 해결되지 않은 문제들이 존재했습니다.

- 참여자의 신뢰도를 판단할 객관적 기준 부재
- 노쇼 및 비매너 유저 관리의 어려움
- 모임 운영 및 정산 과정의 불투명성
- 실시간 소통 부족으로 인한 참여 혼선

모인은 이러한 문제를 해결하기 위해  
**데이터 기반 신뢰 시스템과 실시간 소통 구조를 갖춘 모임 플랫폼**을 기획했습니다.

---

## 💡 해결 방향

> "사람 중심의 모임 문화를, 데이터와 기술로 설계하다."

| | 문제 | 해결 방향 |
|:---:|:---|:---|
| 1️⃣ | 참여자 신뢰도 판단 기준 부재 | 활동 이력 기반 매너 점수 시스템 도입 |
| 2️⃣ | 실시간 소통 부족으로 인한 참여 혼선 | WebSocket + SSE 기반 실시간 채팅 및 알림 구현 |
| 3️⃣ | 정산/환불 과정의 불투명성 | 스케줄러 기반 자동 정산 및 상태 기반 환불 처리 |
| 4️⃣ | 모임 참여 경험의 단절 | 피드 기반 커뮤니티 확장으로 지속적 연결 유도 |

---


# 2. 주요 기능 및 기술

<details>
<summary><strong>📋 주요 기능 상세 보기</strong></summary>
<br>

## 🔒 인증 및 사용자 관리

OAuth2 소셜 로그인과 JWT 기반 인증을 적용하여 서버 확장성을 고려한 Stateless 인증 아키텍처 구현
- OAuth2 소셜 로그인 구현
- JWT AccessToken / RefreshToken 발급
- RefreshToken Redis 저장 및 재발급 로직 구현
- Spring Security 기반 인증 필터 구성

---

## 🤝 신뢰 지표 및 커뮤니티 시스템

사용자의 활동 데이터를 기반으로 행동 중심 데이터 모델링 적용
- 모임 참여, 리뷰, 신고 이력 기반 매너 점수 산정
- 점수 변동 이력 별도 관리
- 크루 생성 및 역할 기반 권한 관리
- 활동 피드 작성, 댓글, 좋아요 기능
- 이미지 파일은 AWS S3에 저장

---

## 👥 크루 및 모임 관리

크루와 모임 도메인을 중심으로 생성, 참여, 운영 흐름을 관리하도록 설계
- 크루 가입 신청, 승인, 거절, 탈퇴 기능 구현
- 모임 신청 및 참여 관리
- Redis를 활용한 크루 찜하기 기능 구현
- 카카오 지도 기반 모임 장소 표시 및 좌표 기반 위치 정보 처리
- Tmap API를 활용한 현재 위치 기준 경로 탐색 기능 구현(대중교통, 차량, 도보)
- 크루원 및 모임원의 가입 상태 관리

---

## 🧑‍🤝‍🧑 모임 및 결제 워크플로우

상태 기반 도메인 로직과 트랜잭션 설계를 통해 데이터 무결성을 보장하도록 설계
- 유/무료 모임 생성
- 결제 성공 시 참여 확정 처리
- 환불 시 상태 롤백 로직 적용
- 동시 신청 상황에서 정원 초과 방지를 위한 동시성 제어

---

## 💬 실시간 메시징 시스템

이벤트 기반 메시징 구조를 적용해 서버 확장 환경에서도 안정적으로 동작하도록 실시간 아키텍처 구현
- WebSocket + STOMP 기반 채팅
- Redis Pub/Sub을 통한 서버 간 메시지 동기화
- 채팅 메시지 비동기 저장
- SSE 기반 실시간 알림
- 주요 이벤트 발생 시 알림 전파 구조 구현

---

## 👮 관리자 및 운영 시스템

플랫폼의 신뢰성과 운영 투명성을 유지하기 위한 관리 기능 설계
- 신고 데이터 기반 콘텐츠 및 사용자 제재 관리
- 신고 누적 시 자동 패널티 처리
- 유료 모임 회비 흐름 모니터링
- 정산 내역 관리 및 운영 투명성 확보

</details>

---

## 💡 핵심 기술

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

# 3. 산출물
<details>
  <summary><b>
    <a href='https://docs.google.com/spreadsheets/d/1GyJp3dJQPYV7_bJa6nXNSuxTnrFpBSRpovNYE-cfyMI/edit?usp=sharing' style="text-decoration: none; color: inherit;"> 
      요구사항 명세서
    </a>
  </b></summary>
  <a href="https://docs.google.com/spreadsheets/d/1GyJp3dJQPYV7_bJa6nXNSuxTnrFpBSRpovNYE-cfyMI/edit?usp=sharing">
</details>

<details>
  <summary><b>
    <a href='https://docs.google.com/spreadsheets/d/1hEHrLjJf3iCyrdZ7XBmxmMM2BLnLyQgs8BqKCxm97bk/edit?usp=sharing' style="text-decoration: none; color: inherit;"> 
      기능 명세서
    </a>
  </b></summary>
  <a href="https://docs.google.com/spreadsheets/d/1hEHrLjJf3iCyrdZ7XBmxmMM2BLnLyQgs8BqKCxm97bk/edit?usp=sharing">
</details>


<details>
  <summary><b> <a href='https://www.erdcloud.com/d/zAo9bewkcJp3R8Zgy' style="text-decoration: none; color: inherit;"> ERD</a></b></summary>
  <img width="1469" height="800" alt="Image" src="https://github.com/user-attachments/assets/1753bd74-cfa5-475f-81d9-a36a303fa44c" />

  <a href="https://www.erdcloud.com/d/zAo9bewkcJp3R8Zgy">
</details>


<details>
  <summary><b> <a href='https://documenter.getpostman.com/view/51059789/2sBXcHhJYf' style="text-decoration: none; color: inherit;"> api 명세서</a></b></summary>
</details>

<details>
  <summary><b> <a href='https://docs.google.com/spreadsheets/d/1PzEMXll7Vz4iZjhTJeZoCeLAkSu9PK_6pxyyyXC5JYs/edit?usp=sharing' style="text-decoration: none; color: inherit;"> WBS</a></b></summary>

</details>

<details>
  <summary><b> <a href='https://www.figma.com/design/yDzBMgiUFMXo8bjLPz25Gh/team4_moin?node-id=0-1&t=nxcIpNQ7qQH028Mg-1'  style="text-decoration: none; color: inherit;"> 피그마</a></b></summary>
</details>

---

# 4. Tech Stack

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
</p>

## 🚀 Infra & DevOps
<p>
  <img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white"/>
  <img src="https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white"/>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
  <img src="https://img.shields.io/badge/AWS_EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white"/>
  <img src="https://img.shields.io/badge/AWS_RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white"/>
  <img src="https://img.shields.io/badge/AWS_S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white"/>
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

# 5. 프로젝트 시연

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

  ![Image](https://github.com/user-attachments/assets/a60dfce0-feca-4a8f-9cae-fab339d0d379)

</details>

<details>
  <summary><b>7-2. 사진 전송, 공지 메시지 및 수정/삭제 지원</b></summary>

  ![Image](https://github.com/user-attachments/assets/bf174c50-a709-41ba-a740-6505cafbac5a)

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
