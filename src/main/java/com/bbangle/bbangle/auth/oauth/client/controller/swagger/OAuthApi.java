package com.bbangle.bbangle.auth.oauth.client.controller.swagger;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.ProfileType;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.UserType;
import com.bbangle.bbangle.common.dto.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "OAuth Login", description = "OAuth 관련 API")
public interface OAuthApi {

    @Operation(
        summary = "OAuth 로그인 (Redirect)",
        description = """
        ### OAuth2 로그인 시작 (Customer & Seller)

        이 엔드포인트는 OAuth2 인증을 시작합니다.
        브라우저에서 접근 시 OAuth2 Provider 로그인 페이지로 이동합니다.
        Customer OAuth2 로그인은 아직 지원하지 않습니다.

        ---
        ### 🔗 OAuth2 로그인 시작 URL - AJAX 호출이 아닌 Redirect를 사용하셔야합니다.
        - `GET /api/v1/oauth/authorization/{OauthServerType}?user={UserType}&profile={ProfileType}`
        - **OauthServerType**은 반드시 **소문자**로 작성하셔야합니다.
        - **user** 파라미터는 **필수**입니다.
        - **profile** 파라미터는 prod가 기본값으로 설정되어있습니다.

        ---
        ### ✅ 로그인 성공 시
        - Redirect URL: `/callback/social?generateToken=1234-abcdefg-567890-hij`
        - 동작:
          1. OAuth2 인증 성공
          2. 임시 코드 (generateToken) 발급
          3. 프론트엔드로 리다이렉트
          4. 프론트엔드의 콜백 페이지에서 임시 코드(generateToken)를 다시 서버로 전송

        ---
        ### ❌ 로그인 실패 시
        - Redirect URL: `/callback/social?error={errorCode}&code={code}`
        - 예시:
          - `/callback/social?error=NOT_SUPPORTED_SERVER&code=-744`
          - `/callback/social?error=INTERNAL_SERVER_ERROR&code=-999`

        ---
        ⚠️ 주의사항
        - JSON 응답을 반환하지 않습니다.
        - Swagger **Try it out**으로 실행하지 마세요.
        - 임시 코드는 URL의 쿼리 파라미터를 통해 전송됩니다.
        - 리다이렉트로 OAuth2 과정을 진행하셔야합니다.
        """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "302",
            description = """
        ### ✅ 로그인 성공 시
        - Redirect URL: `/callback/social?generateToken=1234-abcdefg-567890-hij`
        - 콜백 페이지에서 generateToken을 추출한 다음 Token 발급 API를 호출하시면 됩니다.

        ---
        ### ❌ 로그인 실패 시
        - Redirect URL: `/callback/social?error={errorCode}&code={code}`

        ### ⚠️ 에러 코드 표
        | Error Code | Code | 설명 |
        |-----------|------|------|
        |NOT_SUPPORTED_SERVER|-744|지원하지 않는 OAuth2 로그인 서버입니다.|
        |MISSING_NAME_NICKNAME|-745|이름과 닉네임이 전부 비공개 상태입니다.|
        |INVALID_OAUTH_PARAMS|-748|유효하지 않은 파라미터입니다.|
        |_NOT_SUPPORTED_YET|-993|아직 지원하지 않는 기능입니다.|
        |INTERNAL_SERVER_ERROR|-999|서버 내부 에러입니다. (ex : DB Down)|
        |UNKNOWN_ERROR|null|에러 코드 표에 작성된 Error Code 이외의 기타 예외 상황|
        """
        )
    })
    void login(
        @Parameter(description = "Oauth 서비스 종류", example = "KAKAO, GOOGLE")
        @PathVariable
        OauthServerType oauthServerType,
        @Parameter(description = "로그인 할 계정 종류 - 대소문자 무시", example = "customer, seller")
        @RequestParam("user")
        UserType userType,
        @Parameter(description = "요청한 프론트 환경 - 대소문자 무시, 기본값 = prod", example = "local, prod")
        @RequestParam(value = "profile", required = false, defaultValue = "prod")
        ProfileType profileType
    );

    @Operation(
        summary = "Access Token 재발급",
        description = """
        ### Access Token을 재발급 하고 Refresh Token을 갱신

        ---
        ## ⚠️ 주의사항
        - 서버로 요청을 보낼 때 Refresh Token을 **Cookie**에 담아서 전송해야합니다.
        - 이 때, Cookie의 Key를 **refreshToken**으로 설정해야합니다.
        """
    )
    CommonResult reissueToken(
        @Parameter(description = "Refresh Token")
        @CookieValue(value = "refreshToken", required = false)
        String refreshToken,
        HttpServletResponse response
    );

    @Operation(
        summary = "로그아웃",
        description = """
        ### Refresh Token과 쿠키를 삭제하고 로그아웃 처리

        ---
        ## ⚠️ 주의사항
        - 서버로 요청을 보낼 때 Refresh Token을 **Cookie**에 담아서 전송해야합니다.
        - 이 때, Cookie의 Key를 **refreshToken**으로 설정해야합니다.
        """
    )
    CommonResult logout(
        @Parameter(description = "Refresh Token")
        @CookieValue(value = "refreshToken", required = false)
        String refreshToken,
        HttpServletResponse response
    );
}
