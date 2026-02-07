package com.bbangle.bbangle.common.client.annotation;

import com.bbangle.bbangle.common.client.factory.WithMockUserSecurityContextFactory;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

/**
 * {@code @AuthenticationPrincipal}을 사용하는 Controller 테스트를 위한 커스텀 Mock 인증 어노테이션.
 *
 * <h3>기본값</h3>
 * <ul>
 *     <li>{@code userId}: {@code 1L}</li>
 *     <li>{@code credentials}: {@code ""}</li>
 *     <li>{@code role}: {@code "USER"}</li>
 * </ul>
 *
 * <p>
 * Spring Security의 {@link org.springframework.security.test.context.support.WithMockUser}는
 * {@code principal}에 {@link String} 타입의 username을 주입한다.
 * 이로 인해 Controller에서 아래와 같이 {@code @AuthenticationPrincipal Long}을
 * 사용하는 경우 principal 타입 불일치로 {@code null}이 주입되는 문제가 발생한다.
 * </p>
 *
 * <pre>
 * {@code
 * public Response sampleController(
 *     @AuthenticationPrincipal Long userId
 * ) { ... }
 * }
 * </pre>
 *
 * <p>
 * 본 어노테이션은 테스트 환경에서 {@link org.springframework.security.core.Authentication}의
 * {@code principal}에 {@link Long} 타입의 사용자 ID를 직접 주입하여,
 * 실제 인증 환경(JWT 기반 userId principal 등)과 동일한 동작을 보장한다.
 * </p>
 *
 * <p>
 * 이를 통해 Controller → Facade → Service 흐름에서
 * {@code userId}가 {@code null}로 전달되는 문제를 방지하고,
 * 예외 처리 및 비즈니스 로직을 정확히 검증할 수 있다.
 * </p>
 *
 * <p>
 * 기본적으로 {@code userId=1L}, {@code credentials=""}, {@code role=USER}를 사용하며,
 * 별도 지정이 필요한 경우 테스트 메서드에서 값을 변경할 수 있다.
 * </p>
 *
 * <h3>사용 예시</h3>
 * <pre>
 * {@code
 * @Test
 * @WithMockAuthenticationPrincipal(userId = 1L, credentials="mock-jwt-token", role = "USER")
 * void test_Controller_Code() {
 *     // given
 *     // when
 *     // then
 * }
 * }
 * </pre>
 *
 * @see org.springframework.security.core.annotation.AuthenticationPrincipal
 * @see org.springframework.security.test.context.support.WithMockUser
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockUserSecurityContextFactory.class)
public @interface WithMockAuthenticationPrincipal {

    /**
     * 테스트에서 사용할 사용자 ID (Authentication principal).
     *
     * <p>
     * 기본값은 {@code 1L}이며, 단일 사용자 기준의 Controller 테스트를
     * 간단히 작성할 수 있도록 설정되어 있다.
     * </p>
     */
    long userId() default 1L;

    /**
     * 테스트에서 사용할 인증 정보(credentials).
     *
     * <p>
     * {@link org.springframework.security.core.Authentication#getCredentials()}에 주입되는 값으로,
     * 일반적으로는 JWT 토큰 문자열과 같은 인증 수단을 표현한다.
     * </p>
     *
     * <p>
     * 대부분의 Controller 테스트에서는 credentials를 사용하지 않으므로
     * 기본값은 빈 문자열({@code ""})로 설정되어 있다.
     * </p>
     *
     * <p>
     * 만약 필터나 인터셉터, 또는 인증 정보 기반의 로직을 테스트해야 하는 경우
     * (예: JWT 파싱, 토큰 검증 로직) 이 값을 활용할 수 있다.
     * </p>
     */
    String credentials() default "";

    /**
     * 테스트에서 사용할 사용자 권한 (Role).
     *
     * <p>
     * 기본값은 {@code "USER"}이며,
     * {@code ROLE_} 접두사는 {@link WithMockUserSecurityContextFactory}
     * 에서 자동으로 처리된다.
     * </p>
     */
    String role() default "USER";
}
