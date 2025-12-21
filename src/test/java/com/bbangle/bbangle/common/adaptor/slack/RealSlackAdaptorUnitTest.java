package com.bbangle.bbangle.common.adaptor.slack;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class RealSlackAdaptorUnitTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private RealSlackAdaptor realSlackAdaptor;

    private final String WEB_HOOK_URL = "https://hooks.slack.com/services/dummy/url";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(realSlackAdaptor, "WEB_HOOK_URL", WEB_HOOK_URL);
    }

    @Test
    @DisplayName("sendText가 정상적으로 RestTemplate을 호출하는지 테스트")
    void sendTextTest() {
        String title = "테스트 제목";
        String content = "테스트 내용";

        realSlackAdaptor.sendText(title, content);

           verify(restTemplate).postForEntity(eq(WEB_HOOK_URL), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("sendAlert가 정상적으로 RestTemplate을 호출하는지 테스트")
    void sendAlertTest() {
        Throwable throwable = new RuntimeException("에러 발생"); // 발생한 예외 상황을 가정
        
        when(httpServletRequest.getRequestURI()).thenReturn("/test-uri");

        realSlackAdaptor.sendAlert(httpServletRequest, throwable);

        verify(restTemplate).postForEntity(eq(WEB_HOOK_URL), any(HttpEntity.class), eq(String.class));
    }
}
