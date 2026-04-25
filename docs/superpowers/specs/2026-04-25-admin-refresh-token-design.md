# 관리자 토큰 재발급(Reissue) API 설계 문서

## 배경 및 목적

Bbangle 백엔드의 관리자 인증 흐름은 `POST /api/v1/admin/login` 으로 access(10분) / refresh(14일) 토큰을 함께 반환한다. 그러나 access 토큰이 만료되었을 때 refresh 토큰으로 재발급할 수 있는 엔드포인트가 존재하지 않는다.

- `POST /api/v1/token` — `CustomerTokenService.isCustomer()` 가드로 ROLE_ADMIN 사용 불가
- `POST /api/v1/auth/reissue` — OAuth 쿠키 흐름 기반, admin과 부합하지 않음

access 만료(10분)마다 재로그인이 필요해 운영성이 떨어진다. 본 작업은 관리자 전용 reissue 엔드포인트를 추가해 이 갭을 메운다.

---

## 합의된 설계 결정

| 항목 | 결정 | 이유 |
|------|------|------|
| Refresh Token Rotation | 채택 | 탈취 대응, OAuth 흐름과 일관성 |
| 토큰 전달 방식 | Body JSON | login 응답 패턴과 일관 |
| 검증 강도 | 표준 (서명/만료 → role → DB 일치) | OAuth 패턴과 동일, YAGNI |
| 에러 응답 | 모두 `INVALID_REFRESH_TOKEN(-744, 400)` 통일 | 정보 노출 최소화 |
| 테스트 범위 | 단위 + 통합 | Service 이중 안전장치 |

---

## API 명세

### 엔드포인트

```
POST /api/v1/admin/reissue
```

- **인증 정책**: `PublicApiPath.ANY_METHOD` 에 추가 (만료된 access로도 호출 가능해야 하므로 permitAll)

### Request

```json
{
  "refreshToken": "eyJhbGci..."
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| refreshToken | String | 필수 | 유효한 관리자 refresh 토큰 |

### Response (200 OK)

```json
{
  "status": 200,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci..."
  }
}
```

### Error Response (400 Bad Request)

```json
{
  "status": -744,
  "message": "유효하지 않은 리프레시 토큰입니다."
}
```

---

## 동작 흐름

```
Client → POST /api/v1/admin/reissue {refreshToken}
         ↓
AdminAuthController.reissue(request)
         ↓
AdminAuthService.reissue(request)
  1. tokenProvider.isValidToken(refreshToken)        → false면 INVALID_REFRESH_TOKEN
  2. tokenProvider.parseRefreshToken(refreshToken)   → (adminId, role) 추출
  3. role != ROLE_ADMIN                              → INVALID_REFRESH_TOKEN
  4. refreshTokenRepository.findByRefreshToken(...)  → 없으면 INVALID_REFRESH_TOKEN
  5. 새 accessToken 생성 (10분)
  6. 새 refreshToken 생성 (14일)
  7. RefreshToken.update(newRefreshToken) + save     → Rotation
  8. AdminReissueResponse 반환
         ↓
SingleResult<AdminReissueResponse> {accessToken, refreshToken}
```

---

## 변경 파일

### 신규
- `src/main/java/com/bbangle/bbangle/auth/admin/dto/AdminReissueResponse.java`

### 수정
- `src/main/java/com/bbangle/bbangle/auth/admin/dto/AdminRequest.java` — `AdminReissueRequest` record 추가
- `src/main/java/com/bbangle/bbangle/auth/admin/service/AdminAuthService.java` — `reissue` 메서드 추가
- `src/main/java/com/bbangle/bbangle/auth/admin/controller/AdminAuthController.java` — `POST /reissue` 추가
- `src/main/java/com/bbangle/bbangle/auth/admin/controller/swagger/AdminAuthApi.java` — Swagger 시그니처 추가
- `src/main/java/com/bbangle/bbangle/config/security/PublicApiPath.java` — reissue 경로 추가

### 테스트
- `src/test/java/com/bbangle/bbangle/auth/admin/service/AdminAuthServiceUnitTest.java` — 4 케이스
- `src/test/java/com/bbangle/bbangle/auth/admin/service/AdminAuthServiceIntegrationTest.java` — 1 케이스

---

## 재사용 자산

- `TokenProvider.generateToken / isValidToken / parseRefreshToken`
- `AdminAuthService.ACCESS_TOKEN_DURATION (10분)`, `REFRESH_TOKEN_DURATION (14일)`
- `RefreshTokenRepository.findByRefreshToken / save`
- `RefreshToken.update(newRefreshToken)` 도메인 메서드
- `BbangleErrorCode.INVALID_REFRESH_TOKEN(-744, BAD_REQUEST)` — 신규 에러 코드 불필요
- `ResponseService.getSingleResult(...)`

---

## 보안 고려사항

- **Rotation**: 매 reissue마다 기존 refresh를 새 값으로 교체해 탈취된 토큰을 무력화
- **에러 통일**: 실패 사유를 외부에 노출하지 않아 정보 수집 공격 방어
- **role 검증**: ROLE_ADMIN이 아닌 토큰(Customer refresh 등)으로 admin reissue 불가

---

## Out of Scope

- access 10분 정책 재검토
- refresh blacklist (탈취 즉시 차단)
- `@WebMvcTest` 컨트롤러 슬라이스 테스트 — admin 컨트롤러 테스트 인프라 미구축으로 제외
