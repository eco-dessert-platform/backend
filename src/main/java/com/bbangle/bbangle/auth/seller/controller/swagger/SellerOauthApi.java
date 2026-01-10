package com.bbangle.bbangle.auth.seller.controller.swagger;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Seller Oauth Login", description = "(판매자) 로그인 Oauth API")
public interface SellerOauthApi {

    @Operation(
            summary = "판매자 Oauth 로그인 (Redirect)",
            description = """
        ### OAuth2 로그인 시작 (Admin)

        이 엔드포인트는 OAuth2 인증을 시작합니다.
        브라우저에서 접근 시 OAuth2 Provider 로그인 페이지로 이동합니다.

        ---
        ### 🔗 OAuth2 로그인 시작 URL - AJAX 호출이 아닌 Redirect를 사용하셔야합니다.
        - `GET /api/v1/seller/oauth2/authorization/{OauthServerType}`

        ---
        ### ✅ 로그인 성공 시
        - Redirect URL: `/callback/social?generateToken=1234-abcdefg-567890-hij`
        - 동작:
          1. OAuth2 인증 성공
          2. 인가 코드 (generateToken) 발급
          3. 프론트엔드로 리다이렉트
          4. 프론트엔드의 콜백 페이지에서 인가 코드 (generateToken)를 다시 서버로 전송

        ---
        ### ❌ 로그인 실패 시
        - Redirect URL: `/callback/social?error={errorCode}&code={code}`
        - 예시:
          - `/callback/social?error=NOT_SUPPORTED_SERVER&code=-994`
          - `/callback/social?error=INTERNAL_SERVER_ERROR&code=-999`

        ---
        ⚠️ 주의사항
        - JSON 응답을 반환하지 않습니다.
        - Swagger **Try it out**으로 실행하지 마세요.
        - 인가 코드는 URL의 쿼리 파라미터를 통해 전송됩니다.
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
        |NOT_SUPPORTED_SERVER|-994|지원하지 않는 OAuth2 로그인 서버입니다.|
        |INTERNAL_SERVER_ERROR|-999|서버 내부 에러입니다. (ex : DB Down)|
        |UNKNOWN_ERROR|null|NOT_SUPPORTED_SERVER 또는 INTERNAL_SERVER_ERROR 이외의 Error|
        """
            )
    })
    void sellerLogin(
            @Parameter(description = "Oauth 서비스 종류", example = "KAKAO, GOOGLE")
            @PathVariable("oauthServerType")
            OauthServerType oauthServerType,
            HttpServletResponse response
    );

}
