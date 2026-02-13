package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2Redis;
import com.bbangle.bbangle.common.adaptor.slack.SlackAdaptor;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomFailureHandler implements AuthenticationFailureHandler {

    public static final String OAUTH_STATE_NAMESPACE = "oauth2:params";

    private final SlackAdaptor slackAdaptor;
    private final OAuth2HandlerProperties oauth2HandlerProperties;
    private final RedisRepository redisRepository;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        // TODO : 파라미터를 꺼내기
        String state = request.getParameter(OAuth2ParameterNames.STATE);
        OAuth2Redis.OAuthParams dto = redisRepository.getDTO(OAUTH_STATE_NAMESPACE, state, OAuth2Redis.OAuthParams.class);
        log.debug("CustomFailureHandler Params : {}", dto);

        if (exception instanceof OAuth2Exception ex) {
            BbangleErrorCode code = ex.getCode();

            if (code.getHttpStatus().is5xxServerError()) {
                log.error(ex.getMessage(), ex);
                slackAdaptor.sendAlert(request, ex);
            } else {
                log.warn("OAuth2 Authentication Failed: [{}] {}", code, code.getMessage());
            }

            response.sendRedirect(createRedirectUrl(code));
            return;
        }

        // TODO : 간헐적으로 발생하는 에러를 분석하기 위해 추가 - 추후 삭제 예정
        log.error("Authentication error occurred - FailureHandler | class : {} | message : {} | cause : {}",
            exception.getClass().getName(),
            exception.getMessage(),
            exception.getCause(),
            exception
        );
        
        slackAdaptor.sendAlert(request, exception);
        response.sendRedirect(createRedirectUrl(null));
    }

    private String createRedirectUrl(BbangleErrorCode code) {
        if (code == null) return oauth2HandlerProperties.error() + "?error=" + "UNKNOWN_ERROR";
        return oauth2HandlerProperties.error() + "?error=" + code + "&code=" + code.getCode();
    }
}
