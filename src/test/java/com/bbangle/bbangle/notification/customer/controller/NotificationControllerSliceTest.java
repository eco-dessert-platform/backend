package com.bbangle.bbangle.notification.customer.controller;

import static com.bbangle.bbangle.common.service.ResponseService.CommonResponse.SUCCESS;
import static com.bbangle.bbangle.exception.BbangleErrorCode.NOTIFICATION_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.page.NotificationCustomPage;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.exception.GlobalControllerAdvice;
import com.bbangle.bbangle.notification.customer.dto.NotificationDetailResponseDto;
import com.bbangle.bbangle.notification.customer.dto.NotificationResponse;
import com.bbangle.bbangle.notification.customer.dto.NotificationUploadRequest;
import com.bbangle.bbangle.notification.customer.service.NotificationService;
import com.bbangle.bbangle.token.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.token.jwt.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("[컨트롤러] NotificationController")
@Import({
    TestSlackAdaptorConfig.class,
    JsonDataEncoder.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class
})
@WebMvcTest(controllers = NotificationController.class)
class NotificationControllerSliceTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JsonDataEncoder jsonDataEncoder;

    @SpyBean
    // 실제 빈 객체를 Mockito Spy 객체로 감싸서 기본적으로 실제 메서드가 호출되지만, 필요시 특정 메서드만 가로채서 동작을 정의하거나, 호출 횟수 검증도 가능하다.
    private ResponseService responseService;
    @SpyBean
    private GlobalControllerAdvice globalControllerAdvice;
    @MockBean // 해당 빈을 Mockito Mock 객체로 대체하여, 실제 구현 없이 메서드 호출을 가로채고, 호출 횟수 검증, 동작 정의 등을 할 수 있다.
    private NotificationService notificationService;

    @DisplayName("Notification 페이징 조회 API - 성공")
    @Test
    void givenCursorId_whenGetList_thenReturnNotificationPage() throws Exception {
        // Given
        Long cursorId = 1L;
        NotificationResponse response = new NotificationResponse(1L, "title1", "content1",
            "2023-10-01 12:00");
        NotificationCustomPage<List<NotificationResponse>> page =
            NotificationCustomPage.from(List.of(response), 2L);
        given(notificationService.getList(cursorId)).willReturn(page);

        // When & Then
        mvc.perform(get("/api/v1/notification")
                .param("cursorId", cursorId.toString())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
            .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()))
            .andExpect(jsonPath("$.result.content[0].id").value(1))
            .andExpect(jsonPath("$.result.content[0].title").value(response.title()))
            .andExpect(jsonPath("$.result.content[0].content").value(response.content()))
            .andExpect(jsonPath("$.result.content[0].createdAt").value(response.createdAt()))
            .andExpect(jsonPath("$.result.nextCursor").value(page.getNextCursor()))
            .andExpect(jsonPath("$.result.hasNext").value(page.getHasNext()));
        then(notificationService).should(times(1)).getList(cursorId);
        then(responseService).should(times(1)).getSingleResult(page);
    }

    @DisplayName("Notification 상세 조회 API - 성공")
    @Test
    void givenNotificationId_whenGetNoticeDetail_thenReturnNotificationDetail() throws Exception {
        // Given
        Long id = 1L;
        NotificationDetailResponseDto detailResponse = new NotificationDetailResponseDto(
            id, "title1", "content1", "2023-10-01 12:00");
        given(notificationService.getNoticeDetail(id)).willReturn(detailResponse);

        // When & Then
        mvc.perform(get("/api/v1/notification/{id}", id)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
            .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()))
            .andExpect(jsonPath("$.result.id").value(id))
            .andExpect(jsonPath("$.result.title").value(detailResponse.title()))
            .andExpect(jsonPath("$.result.content").value(detailResponse.content()))
            .andExpect(jsonPath("$.result.createdAt").value(detailResponse.createdAt()));
        then(notificationService).should(times(1)).getNoticeDetail(id);
        then(responseService).should(times(1)).getSingleResult(detailResponse);
    }

    @DisplayName("Notification 상세 조회 API - 실패(존재하지 않는 ID)")
    @Test
    void givenNonExistingNotificationId_whenGetNoticeDetail_thenReturns4xxClientError()
        throws Exception {
        // Given
        Long nonExistingId = -1L;
        given(notificationService.getNoticeDetail(nonExistingId)).willThrow(
            new BbangleException(NOTIFICATION_NOT_FOUND));

        // When & Then
        mvc.perform(get("/api/v1/notification/{id}", nonExistingId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(NOTIFICATION_NOT_FOUND.getCode()))
            .andExpect(jsonPath("$.message").value(NOTIFICATION_NOT_FOUND.getMessage()));

        then(notificationService).should(times(1)).getNoticeDetail(nonExistingId);
        then(globalControllerAdvice).should(times(1))
            .handleBbangleException(any(HttpServletRequest.class), any(BbangleException.class));
        then(responseService).should(times(1)).getFailResult(anyString(), anyInt());
    }

    @DisplayName("Notification 등록 API - 성공")
    @Test
    void givenNotificationUploadRequest_whenUpload_thenReturnsSuccess() throws Exception {
        // Given
        NotificationUploadRequest uploadRequest = new NotificationUploadRequest("title1",
            "content1");
        willDoNothing().given(notificationService).upload(any(NotificationUploadRequest.class));

        // When & Then
        mvc.perform(post("/api/v1/notification")
                .with(user("user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDataEncoder.encode(uploadRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
            .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()));
        then(notificationService).should(times(1)).upload(any(NotificationUploadRequest.class));
        then(responseService).should(times(1)).getSuccessResult();
    }

    @DisplayName("Notification 등록 API - 실패(권한 없는 사용자)")
    @Test
    void givenNoAuthority_whenUploadNotification_thenReturns4xxClientError() throws Exception {
        // Given
        NotificationUploadRequest uploadRequest = new NotificationUploadRequest("title1",
            "content1");

        // When & Then
        mvc.perform(post("/api/v1/notification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDataEncoder.encode(uploadRequest)))
            .andExpect(status().is4xxClientError());
    }

}
