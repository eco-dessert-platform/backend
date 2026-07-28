---
name: test-write
description: 이 프로젝트의 테스트 작성 컨벤션(BDD Mockito 스타일, given/when/then, Fixture 패턴, 테스트 유형별 구조)을 다룬다. 컨트롤러/서비스/통합/JPA 테스트를 작성할 때 사용한다.
---

# 테스트 작성 가이드

## 공통 규칙

- **static import 필수**: `BDDMockito.given(...)`, `Assertions.assertThat(...)` 처럼 풀네임으로 쓰지 않는다. 항상 static import 사용.
  ```java
  import static org.assertj.core.api.Assertions.assertThat;
  import static org.mockito.BDDMockito.given;
  ```
- **Mockito는 BDD 스타일**: `when().thenReturn()` 대신 `given().willReturn()/willThrow()`, `verify()` 대신 필요 시 `then().should()` 사용.
- **given/when/then 주석 3단 구성**을 각 테스트 메서드에 정확히 단다 (`// given`, `// when`, `// then`). 컨트롤러 테스트는 `// when & then`으로 합쳐도 됨.
- **assertj**: `assertThat(...)`, 예외는 `assertThatThrownBy(...).isInstanceOf(BbangleException.class).satisfies(...)`, 다중 필드 비교는 `.extracting(...)` / `tuple(...)`.
- **Fixture**: `src/test/java/.../fixture/{도메인}/domain/XxxFixture.java`에 도메인별로 둔다. private 생성자 + `public static final` 상수(DEFAULT_*, NEW_*) + `defaultXxx()` / `defaultXxx(param)` static 팩토리 메서드로 Lombok builder를 감싼다. FixtureMonkey는 쓰지 않는다.

---

## 컨트롤러 테스트

`@WebMvcTest(controllers = X.class)` + 필요한 설정만 `@Import({...})` + `@ActiveProfiles("test")`. `@MockBean`으로 Service/Facade 목킹. `@Nested` + 한글 `@DisplayName`으로 메서드별 그룹핑. 인증이 필요하면 `@WithMockAuthenticationPrincipal(role = "SELLER")` 사용. 검증은 `mockMvc.perform().andExpect(jsonPath(...))` 체이닝.

## 비즈니스 단위/mock 테스트 (`*ServiceUnitTest`)

`@ExtendWith(MockitoExtension.class)` + `@Mock`(의존성) + `@InjectMocks`(대상). 별도 베이스 클래스 없음. `assertThat(result)...` 로 상태 검증 + `verify(mock).method(...)`로 상호작용 검증을 함께 한다.

## 비즈니스 통합 테스트 (`*FacadeIntegrationTest` / `*IntegrationTest`)

`@SpringBootTest` + `@ActiveProfiles("test")` + `@Transactional`(자동 롤백). S3 연동이 있으면 `S3IntegrationTestSupport`를 상속. Mock 없이 실제 빈 조합으로 동작시키고, Fixture로 만든 엔티티를 Repository에 `saveAndFlush`로 저장한 뒤 대상 Facade/Service를 실행한다.

## Data JPA 테스트 (`*RepositoryTest`)

`@DataJpaTest` + `@AutoConfigureTestDatabase(replace = Replace.NONE)`(임베디드로 교체하지 않고 TestContainers 실DB 사용) + `@Import({TestContainersConfig.class, QueryDslConfig.class, ...})` + `@Transactional` + `@ActiveProfiles("test")`. `TestContainersConfig`가 static 블록으로 MariaDB/Redis/LocalStack을 1회만 기동하고 `@ServiceConnection`으로 연결하므로, 새 베이스 클래스를 만들지 말고 이 설정을 `@Import`만 하면 된다.
