package com.bbangle.bbangle.auth.seller.controller.swagger;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.seller.controller.dto.GenerateTokenResponse;
import com.bbangle.bbangle.common.dto.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
        - **OauthServerType**은 반드시 **소문자**로 작성하셔야합니다.

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
          - `/callback/social?error=NOT_SUPPORTED_SERVER&code=-744`
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
        |NOT_SUPPORTED_SERVER|-744|지원하지 않는 OAuth2 로그인 서버입니다.|
        |MISSING_NAME_NICKNAME|-745|이름 또는 닉네임이 비공개 상태입니다.|
        |INTERNAL_SERVER_ERROR|-999|서버 내부 에러입니다. (ex : DB Down)|
        |UNKNOWN_ERROR|null|에러 코드 표에 작성된 Error Code 이외의 기타 예외 상황|
        """
        )
    })
    void sellerLogin(
        @Parameter(description = "Oauth 서비스 종류", example = "KAKAO, GOOGLE")
        @PathVariable("oauthServerType") OauthServerType oauthServerType
    );

    @Operation(
        summary = "판매자 Token 발급 및 계정 상태 조회",
        description = """
        ### 판매자의 JWT 토큰을 발급 받고 계정 상태를 조회

        ---
        ### 판매자 계정 상태 설명 표
        |Status Code|설명|
        |-----------|----|
        |NEW|아직 사업자 정보를 **제출하지 않은 상태**|
        |PENDING|사업자 정보를 제출하였지만 **심사 중**인 상태|
        |APPROVED|관리자가 해당 사업자 정보를 **승인**한 상태|
        |REJECTED|부적합하다고 판단되어 사업자 정보를 **승인 거부**한 상태|
        """
    )
    SingleResult<GenerateTokenResponse> sellerToken(
        @RequestParam(value = "generateToken", required = false)
        @NotBlank(message = "generateToken은 필수입니다.")
        String code
    );
}
