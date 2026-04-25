# 관리자 토큰 재발급 API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `POST /api/v1/admin/reissue` 엔드포인트를 추가해 관리자가 만료된 access 토큰을 refresh 토큰으로 재발급받을 수 있게 한다.

**Architecture:** `AdminAuthService.reissue` 메서드가 refresh 검증(서명/role/DB 일치) → 새 access/refresh 생성 → Rotation(기존 row update)을 수행한다. 실패 케이스는 모두 `INVALID_REFRESH_TOKEN(-744, 400)` 으로 통일한다.

**Tech Stack:** Spring Boot 3.2.1, Java 17, jjwt(JWT), Spring Data JPA, MockitoExtension(단위 테스트), TestContainers+SpringBootTest(통합 테스트)

---

## 파일 구조

| 파일 | 변경 | 역할 |
|------|------|------|
| `auth/admin/dto/AdminReissueResponse.java` | 신규 | reissue 응답 DTO |
| `auth/admin/dto/AdminRequest.java` | 수정 | `AdminReissueRequest` record 추가 |
| `auth/admin/service/AdminAuthService.java` | 수정 | `reissue(...)` 메서드 추가 |
| `auth/admin/controller/AdminAuthController.java` | 수정 | `POST /reissue` 엔드포인트 추가 |
| `auth/admin/controller/swagger/AdminAuthApi.java` | 수정 | Swagger 시그니처 추가 |
| `config/security/PublicApiPath.java` | 수정 | `/api/v1/admin/reissue` permitAll 추가 |
| `test/.../AdminAuthServiceUnitTest.java` | 수정 | reissue 단위 테스트 4 케이스 추가 |
| `test/.../AdminAuthServiceIntegrationTest.java` | 수정 | reissue 통합 테스트 1 케이스 추가 |

모든 파일의 절대 경로 베이스: `src/main/java/com/bbangle/bbangle/` / `src/test/java/com/bbangle/bbangle/`

---

### Task 1: 단위 테스트 작성 (Red)

**Files:**
- Modify: `src/test/java/com/bbangle/bbangle/auth/admin/service/AdminAuthServiceUnitTest.java`

> 아직 `AdminAuthService.reissue` 메서드가 없으므로 컴파일 에러가 발생하는 것이 정상(Red 상태).

- [ ] **Step 1: reissue 관련 import 추가**

`AdminAuthServiceUnitTest.java` 상단 import 블록에 아래를 추가한다:

```java
import com.bbangle.bbangle.auth.admin.dto.AdminReissueResponse;
import com.bbangle.bbangle.auth.admin.dto.AdminRequest.AdminReissueRequest;
import com.bbangle.bbangle.auth.oauth.client.dto.TokenClaimsDTO;
import com.bbangle.bbangle.exception.BbangleErrorCode;
```

- [ ] **Step 2: 단위 테스트 4 케이스 추가**

기존 `logoutSuccess()` 테스트 아래에 다음 4개 테스트를 추가한다:

```java
@Test
@DisplayName("reissue 성공 시 새 토큰을 반환하고 RefreshToken을 갱신한다")
void reissueSuccess() {
    // given
    String oldRefreshToken = "oldRefreshToken";
    RefreshToken storedToken = RefreshToken.create(1L, Role.ROLE_ADMIN, oldRefreshToken);
    TokenClaimsDTO claims = TokenClaimsDTO.builder()
            .id(1L)
            .role(Role.ROLE_ADMIN)
            .build();

    given(tokenProvider.isValidToken(oldRefreshToken)).willReturn(true);
    given(tokenProvider.parseRefreshToken(oldRefreshToken)).willReturn(claims);
    given(refreshTokenRepository.findByRefreshToken(oldRefreshToken)).willReturn(Optional.of(storedToken));
    given(tokenProvider.generateToken(1L, Role.ROLE_ADMIN, AdminAuthService.ACCESS_TOKEN_DURATION))
            .willReturn("newAccessToken");
    given(tokenProvider.generateToken(1L, Role.ROLE_ADMIN, AdminAuthService.REFRESH_TOKEN_DURATION))
            .willReturn("newRefreshToken");

    // when
    AdminReissueResponse response = adminAuthService.reissue(new AdminReissueRequest(oldRefreshToken));

    // then
    assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
    assertThat(response.getRefreshToken()).isEqualTo("newRefreshToken");
    verify(refreshTokenRepository).save(any(RefreshToken.class));
}

@Test
@DisplayName("reissue 시 유효하지 않은 토큰이면 예외가 발생한다")
void reissueFail_invalidToken() {
    // given
    String invalidToken = "invalidToken";
    given(tokenProvider.isValidToken(invalidToken)).willReturn(false);

    // when & then
    assertThatThrownBy(() -> adminAuthService.reissue(new AdminReissueRequest(invalidToken)))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining("유효하지 않은 리프레시 토큰입니다.");
}

@Test
@DisplayName("reissue 시 role이 ROLE_ADMIN이 아니면 예외가 발생한다")
void reissueFail_notAdminRole() {
    // given
    String customerRefreshToken = "customerRefreshToken";
    TokenClaimsDTO claims = TokenClaimsDTO.builder()
            .id(1L)
            .role(Role.ROLE_CUSTOMER)
            .build();

    given(tokenProvider.isValidToken(customerRefreshToken)).willReturn(true);
    given(tokenProvider.parseRefreshToken(customerRefreshToken)).willReturn(claims);

    // when & then
    assertThatThrownBy(() -> adminAuthService.reissue(new AdminReissueRequest(customerRefreshToken)))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining("유효하지 않은 리프레시 토큰입니다.");
}

@Test
@DisplayName("reissue 시 DB에 refresh가 존재하지 않으면 예외가 발생한다")
void reissueFail_notFoundInDb() {
    // given
    String unknownRefreshToken = "unknownRefreshToken";
    TokenClaimsDTO claims = TokenClaimsDTO.builder()
            .id(1L)
            .role(Role.ROLE_ADMIN)
            .build();

    given(tokenProvider.isValidToken(unknownRefreshToken)).willReturn(true);
    given(tokenProvider.parseRefreshToken(unknownRefreshToken)).willReturn(claims);
    given(refreshTokenRepository.findByRefreshToken(unknownRefreshToken)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> adminAuthService.reissue(new AdminReissueRequest(unknownRefreshToken)))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining("유효하지 않은 리프레시 토큰입니다.");
}
```

- [ ] **Step 3: 컴파일 에러 확인 (Red 검증)**

```bash
./gradlew compileTestJava 2>&1 | grep -A 3 "error:"
```

Expected: `cannot find symbol` — `AdminReissueRequest`, `AdminReissueResponse`, `adminAuthService.reissue` 관련 에러

---

### Task 2: DTO 생성 (AdminReissueRequest, AdminReissueResponse)

**Files:**
- Modify: `src/main/java/com/bbangle/bbangle/auth/admin/dto/AdminRequest.java`
- Create: `src/main/java/com/bbangle/bbangle/auth/admin/dto/AdminReissueResponse.java`

- [ ] **Step 1: AdminRequest.java에 AdminReissueRequest record 추가**

`AdminRequest.java` 의 `AdminLoginRequest` record 아래에 다음을 추가한다:

```java
public record AdminReissueRequest(
        @Schema(description = "관리자 리프레시 토큰")
        @NotBlank(message = "리프레시 토큰은 필수입니다.")
        String refreshToken
) {
}
```

추가 후 전체 파일은 다음과 같다:

```java
package com.bbangle.bbangle.auth.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class AdminRequest {

    public record AdminLoginRequest(
            @Schema(description = "관리자 계정 ID")
            @NotBlank(message = "관리자 계정 ID는 필수입니다.")
            String accountId,

            @Schema(description = "관리자 계정 비밀번호")
            @NotBlank(message = "관리자 계정 비밀번호는 필수입니다.")
            String password
    ) {
    }

    public record AdminReissueRequest(
            @Schema(description = "관리자 리프레시 토큰")
            @NotBlank(message = "리프레시 토큰은 필수입니다.")
            String refreshToken
    ) {
    }

}
```

- [ ] **Step 2: AdminReissueResponse.java 생성**

```java
package com.bbangle.bbangle.auth.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReissueResponse {

    private String accessToken;
    private String refreshToken;

}
```

- [ ] **Step 3: 컴파일 확인**

```bash
./gradlew compileJava 2>&1 | grep -i "error"
```

Expected: 에러 없음 (DTO만으로는 컴파일 통과)

---

### Task 3: AdminAuthService.reissue 구현 (Green)

**Files:**
- Modify: `src/main/java/com/bbangle/bbangle/auth/admin/service/AdminAuthService.java`

- [ ] **Step 1: reissue 메서드 추가**

`AdminAuthService.java` 의 `logout` 메서드 아래에 다음을 추가한다:

```java
@Transactional
public AdminReissueResponse reissue(AdminRequest.AdminReissueRequest request) {
    String refreshTokenVal = request.refreshToken();

    if (!tokenProvider.isValidToken(refreshTokenVal)) {
        throw new BbangleException(BbangleErrorCode.INVALID_REFRESH_TOKEN);
    }

    TokenClaimsDTO claims = tokenProvider.parseRefreshToken(refreshTokenVal);

    if (claims.role() != Role.ROLE_ADMIN) {
        throw new BbangleException(BbangleErrorCode.INVALID_REFRESH_TOKEN);
    }

    RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(refreshTokenVal)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.INVALID_REFRESH_TOKEN));

    String newAccessToken = tokenProvider.generateToken(claims.id(), Role.ROLE_ADMIN, ACCESS_TOKEN_DURATION);
    String newRefreshToken = tokenProvider.generateToken(claims.id(), Role.ROLE_ADMIN, REFRESH_TOKEN_DURATION);

    refreshToken.update(newRefreshToken);
    refreshTokenRepository.save(refreshToken);

    return AdminReissueResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .build();
}
```

- [ ] **Step 2: import 추가**

`AdminAuthService.java` 상단 import 블록에 누락된 import를 추가한다:

```java
import com.bbangle.bbangle.auth.admin.dto.AdminReissueResponse;
import com.bbangle.bbangle.auth.oauth.client.dto.TokenClaimsDTO;
```

- [ ] **Step 3: 단위 테스트 실행 (Green 검증)**

```bash
./gradlew test --tests "com.bbangle.bbangle.auth.admin.service.AdminAuthServiceUnitTest" 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL, 모든 테스트 PASS (기존 5개 + 신규 4개 = 9개)

- [ ] **Step 4: 커밋**

```bash
git add src/main/java/com/bbangle/bbangle/auth/admin/dto/AdminReissueResponse.java \
        src/main/java/com/bbangle/bbangle/auth/admin/dto/AdminRequest.java \
        src/main/java/com/bbangle/bbangle/auth/admin/service/AdminAuthService.java \
        src/test/java/com/bbangle/bbangle/auth/admin/service/AdminAuthServiceUnitTest.java
git commit -m "[Feat] 관리자 토큰 재발급 Service 및 단위 테스트 추가"
```

---

### Task 4: Controller / Swagger / PublicApiPath 추가

**Files:**
- Modify: `src/main/java/com/bbangle/bbangle/auth/admin/controller/AdminAuthController.java`
- Modify: `src/main/java/com/bbangle/bbangle/auth/admin/controller/swagger/AdminAuthApi.java`
- Modify: `src/main/java/com/bbangle/bbangle/config/security/PublicApiPath.java`

- [ ] **Step 1: PublicApiPath에 reissue 경로 추가**

`PublicApiPath.java` 의 `ANY_METHOD` 배열에 `AdminApiPath.PREFIX + "/reissue"` 를 추가한다:

```java
public static final String[] ANY_METHOD = {
    "/api/v1/token",
    AdminApiPath.PREFIX + "/login",
    AdminApiPath.PREFIX + "/logout",
    AdminApiPath.PREFIX + "/reissue",   // 추가
    "/api/v1/oauth/**",
    "/api/v1/search/**",
    "/api/v1/landingpage",
    "/api/v1/store/**",
    "/api/v1/stores/**",
    "/api/v1/health/**",
    "/api/v1/push/**",
    SellerApiPath.PREFIX + "/oauth/tokens",
    PublicApiPath.AUTH_PREFIX + "/**"
};
```

- [ ] **Step 2: AdminAuthApi 인터페이스에 reissue 시그니처 추가**

`AdminAuthApi.java` 의 `logout` 메서드 아래에 다음을 추가한다:

```java
@Operation(summary = "관리자 토큰 재발급", description = "만료된 access 토큰을 refresh 토큰으로 재발급합니다.")
SingleResult<AdminReissueResponse> reissue(
        AdminReissueRequest request
);
```

추가 후 import가 필요하다:

```java
import com.bbangle.bbangle.auth.admin.dto.AdminReissueResponse;
import com.bbangle.bbangle.auth.admin.dto.AdminRequest.AdminReissueRequest;
```

- [ ] **Step 3: AdminAuthController에 reissue 엔드포인트 추가**

`AdminAuthController.java` 의 `logout` 메서드 아래에 다음을 추가한다:

```java
@Override
@PostMapping("/reissue")
public SingleResult<AdminReissueResponse> reissue(@RequestBody AdminReissueRequest request) {
    return responseService.getSingleResult(adminAuthService.reissue(request));
}
```

추가 후 import가 필요하다:

```java
import com.bbangle.bbangle.auth.admin.dto.AdminReissueResponse;
import com.bbangle.bbangle.auth.admin.dto.AdminRequest.AdminReissueRequest;
```

- [ ] **Step 4: 컴파일 확인**

```bash
./gradlew compileJava 2>&1 | grep -i "error"
```

Expected: 에러 없음

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/bbangle/bbangle/auth/admin/controller/AdminAuthController.java \
        src/main/java/com/bbangle/bbangle/auth/admin/controller/swagger/AdminAuthApi.java \
        src/main/java/com/bbangle/bbangle/config/security/PublicApiPath.java
git commit -m "[Feat] 관리자 토큰 재발급 Controller 및 Security 설정 추가"
```

---

### Task 5: 통합 테스트 추가 및 전체 검증

**Files:**
- Modify: `src/test/java/com/bbangle/bbangle/auth/admin/service/AdminAuthServiceIntegrationTest.java`

- [ ] **Step 1: import 추가**

`AdminAuthServiceIntegrationTest.java` 상단에 다음을 추가한다:

```java
import com.bbangle.bbangle.auth.admin.dto.AdminReissueResponse;
import com.bbangle.bbangle.auth.admin.dto.AdminRequest.AdminReissueRequest;
```

- [ ] **Step 2: reissue 통합 테스트 케이스 추가**

기존 `logout_success()` 테스트 아래에 다음을 추가한다:

```java
@Test
@DisplayName("reissue 성공 시 새 access/refresh 토큰이 발급되고 refresh_token row가 갱신된다")
void reissue_success() {
    // arrange
    String rawPassword = "password";
    String encoded = passwordEncoder.encode(rawPassword);

    Admin admin = Admin.builder()
            .accountId("adminReissue")
            .password(encoded)
            .name("Admin")
            .build();
    adminRepository.saveAndFlush(admin);

    AdminLoginRequest loginRequest = new AdminLoginRequest("adminReissue", rawPassword);
    AdminLoginResponse loginResponse = adminAuthService.login(loginRequest);

    String originalRefreshToken = loginResponse.getRefreshToken();

    // act
    AdminReissueResponse reissueResponse = adminAuthService.reissue(
            new AdminReissueRequest(originalRefreshToken)
    );

    // assert — 새 토큰이 발급됨
    assertThat(reissueResponse.getAccessToken()).isNotNull();
    assertThat(reissueResponse.getRefreshToken()).isNotNull();
    assertThat(reissueResponse.getAccessToken()).isNotEqualTo(loginResponse.getAccessToken());
    assertThat(reissueResponse.getRefreshToken()).isNotEqualTo(originalRefreshToken);

    // assert — DB의 refresh row가 새 값으로 갱신됨 (Rotation)
    assertThat(refreshTokenRepository.findByRefreshToken(reissueResponse.getRefreshToken())).isPresent();
    assertThat(refreshTokenRepository.findByRefreshToken(originalRefreshToken)).isEmpty();
}
```

- [ ] **Step 3: 통합 테스트 실행**

```bash
./gradlew test --tests "com.bbangle.bbangle.auth.admin.service.AdminAuthServiceIntegrationTest" 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL, 기존 2개 + 신규 1개 = 3개 PASS

- [ ] **Step 4: 전체 admin auth 테스트 실행**

```bash
./gradlew test --tests "com.bbangle.bbangle.auth.admin.*" 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 5: 전체 회귀 테스트**

```bash
./gradlew test 2>&1 | tail -30
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Jacoco 커버리지 검증**

```bash
./gradlew jacocoTestCoverageVerification 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL (LINE/BRANCH/CLASS 70% 기준 통과)

- [ ] **Step 7: 커밋**

```bash
git add src/test/java/com/bbangle/bbangle/auth/admin/service/AdminAuthServiceIntegrationTest.java \
        docs/superpowers/specs/2026-04-25-admin-refresh-token-design.md \
        docs/superpowers/plans/2026-04-25-admin-refresh-token.md
git commit -m "[Feat] 관리자 토큰 재발급 통합 테스트 추가 및 문서화"
```

---

## 검증 포인트 요약

| 항목 | 명령 | 기대 결과 |
|------|------|----------|
| 단위 테스트 | `./gradlew test --tests "*.AdminAuthServiceUnitTest"` | 9개 PASS |
| 통합 테스트 | `./gradlew test --tests "*.AdminAuthServiceIntegrationTest"` | 3개 PASS |
| 전체 회귀 | `./gradlew test` | BUILD SUCCESSFUL |
| 커버리지 | `./gradlew jacocoTestCoverageVerification` | BUILD SUCCESSFUL |
