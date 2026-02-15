package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2Redis.OAuthParams;
import com.bbangle.bbangle.common.adaptor.slack.SlackAdaptor;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomFailureHandler implements AuthenticationFailureHandler {

    public static final String OAUTH_STATE_NAMESPACE = "oauth2:params";

    private final SlackAdaptor slackAdaptor;
    private final OAuth2HandlerProperties oAuth2HandlerProperties;
    private final RedisRepository redisRepository;

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException {

        Optional<OAuthParams> optionalParams = extractParams(request);

        if (optionalParams.isEmpty()) {
            redirectInvalidParams(response);
            return;
        }

        OAuthParams params = optionalParams.get();

        if (exception instanceof OAuth2Exception oAuth2Exception) {
            handleOAuth2Exception(request, response, params, oAuth2Exception);
            return;
        }

        handleUnknownException(request, response, params, exception);
    }

    private Optional<OAuthParams> extractParams(HttpServletRequest request) {
        String state = request.getParameter("state");
        if (state == null) {
            return Optional.empty();
        }

        OAuthParams dto = redisRepository.getDTOAndDelete(OAUTH_STATE_NAMESPACE, state, OAuthParams.class);

        return Optional.ofNullable(dto);
    }

    private void handleOAuth2Exception(
        HttpServletRequest request,
        HttpServletResponse response,
        OAuthParams params,
        OAuth2Exception exception
    ) throws IOException {
        BbangleErrorCode code = exception.getCode();

        if (code.getHttpStatus().is5xxServerError()) {
            log.error(exception.getMessage(), exception);
            slackAdaptor.sendAlert(request, exception);
        } else {
            log.warn("OAuth2 Authentication Failed: [{}] {}", code, code.getMessage());
        }

        if (code == BbangleErrorCode.OAUTH_INVALID_PARAMS) {
            redirectInvalidParams(response);
            return;
        }

        response.sendRedirect(oAuth2HandlerProperties.getErrorUrl(params, code));
    }

    private void handleUnknownException(
        HttpServletRequest request,
        HttpServletResponse response,
        OAuthParams params,
        AuthenticationException exception
    ) throws IOException {
        log.error("Authentication error occurred", exception);
        slackAdaptor.sendAlert(request, exception);

        response.sendRedirect(oAuth2HandlerProperties.getErrorUrl(params, null));
    }

    private void redirectInvalidParams(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth.html?error=" + BbangleErrorCode.OAUTH_INVALID_PARAMS.name());
    }
}
