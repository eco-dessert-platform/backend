package com.bbangle.bbangle.notification.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.config.security.AdminApiPath;
import com.bbangle.bbangle.config.security.SecurityConfig;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationCreateRequest;
import com.bbangle.bbangle.notification.admin.facade.AdminNotificationFacade;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeInfo.NoticeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@DisplayName("[컨트롤러 테스트] AdminNotificationController")
@WebMvcTest(controllers = AdminNotificationController.class)
@Import({
    TestSlackAdaptorConfig.class,
    JsonDataEncoder.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class,
    SecurityConfig.class
})
@ActiveProfiles("test")
public class AdminNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminNotificationFacade adminNotificationFacade;


    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("정상 요청으로 공지사항 생성에 성공한다")
    void success_registerNotification_WithValidInput() throws Exception {
        // given
        Long adminId = 1L;

        AdminNotificationCreateRequest createRequest = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "<div>공지사항 본문 HTML</div>"
        );

        MockMultipartFile image = new MockMultipartFile(
            "profileImage",
            "uuid-1",
            MediaType.IMAGE_JPEG_VALUE,
            "image content".getBytes()
        );

        NoticeInfo mockResponse = NoticeInfo.builder()
            .id(1L)
            .title("공지사항 제목")
            .content("<div>공지사항 본문 HTML</div>")
            .imageLinks(List.of("https://cdn.example.com/image1.jpg"))
            .createAt(LocalDateTime.now())
            .modifiedAt(LocalDateTime.now())
            .build();

        when(adminNotificationFacade.createNotice(eq(adminId), any(AdminNotificationCreateRequest.class), anyList()))
            .thenReturn(mockResponse);

        String requestJson = objectMapper.writeValueAsString(createRequest);
        MockMultipartFile requestPart = new MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            requestJson.getBytes(StandardCharsets.UTF_8)
        );

        // when & then
        mockMvc.perform(
                MockMvcRequestBuilders.multipart(AdminApiPath.PREFIX + "/notifications/" + adminId + "/register")
                    .file(image)
                    .file(requestPart)
            )
            .andDo(print())
            .andExpect(status().isOk());

        // Facade 호출 검증
        verify(adminNotificationFacade).createNotice(eq(adminId), any(AdminNotificationCreateRequest.class), anyList());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("제목이 비어있으면 공지사항 생성에 실패한다")
    void registerNotification_Fails_WithBlankTitle() throws Exception {
        // given
        Long adminId = 1L;

        AdminNotificationCreateRequest createRequest = new AdminNotificationCreateRequest(
            "", // 빈 제목
            "<div>공지사항 본문 HTML</div>"
        );

        MockMultipartFile image = new MockMultipartFile(
            "profileImage",
            "image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "image content".getBytes()
        );

        String requestJson = objectMapper.writeValueAsString(createRequest);
        MockMultipartFile requestPart = new MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            requestJson.getBytes(StandardCharsets.UTF_8)
        );

        // when & then
        mockMvc.perform(
                MockMvcRequestBuilders.multipart(AdminApiPath.PREFIX + "/notifications/" + adminId + "/register")
                    .file(image)
                    .file(requestPart)
            )
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("본문이 비어있으면 공지사항 생성에 실패한다")
    void registerNotification_Fails_WithBlankContent() throws Exception {
        // given
        Long adminId = 1L;

        AdminNotificationCreateRequest createRequest = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "" // 빈 본문
        );

        MockMultipartFile image = new MockMultipartFile(
            "profileImage",
            "image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "image content".getBytes()
        );

        String requestJson = objectMapper.writeValueAsString(createRequest);
        MockMultipartFile requestPart = new MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            requestJson.getBytes(StandardCharsets.UTF_8)
        );

        // when & then
        mockMvc.perform(
                MockMvcRequestBuilders.multipart(AdminApiPath.PREFIX + "/notifications/" + adminId + "/register")
                    .file(image)
                    .file(requestPart)
            )
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("권한 없으면 공지사항 생성에 실패한다 (403 Forbidden)")
    void registerNotification_Fails_WithoutAdminRole() throws Exception {
        // given
        Long adminId = 1L;

        AdminNotificationCreateRequest createRequest = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "<div>공지사항 본문 HTML</div>"
        );

        MockMultipartFile image = new MockMultipartFile(
            "profileImage",
            "image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "image content".getBytes()
        );

        String requestJson = objectMapper.writeValueAsString(createRequest);
        MockMultipartFile requestPart = new MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            requestJson.getBytes(StandardCharsets.UTF_8)
        );

        // when & then - 로그인하지 않은 사용자
        mockMvc.perform(
                MockMvcRequestBuilders.multipart(AdminApiPath.PREFIX + "/notifications/" + adminId + "/register")
                    .file(image)
                    .file(requestPart)
            )
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("복수 이미지로 공지사항 생성에 성공한다")
    void success_registerNotification_WithMultipleImages() throws Exception {
        // given
        Long adminId = 1L;

        AdminNotificationCreateRequest createRequest = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "<div>공지사항 본문 HTML</div>"
        );

        MockMultipartFile image1 = new MockMultipartFile(
            "profileImage",
            "uuid-1",
            MediaType.IMAGE_JPEG_VALUE,
            "image1 content".getBytes()
        );

        MockMultipartFile image2 = new MockMultipartFile(
            "profileImage",
            "uuid-2",
            MediaType.IMAGE_JPEG_VALUE,
            "image2 content".getBytes()
        );

        NoticeInfo mockResponse = NoticeInfo.builder()
            .id(1L)
            .title("공지사항 제목")
            .content("<div>공지사항 본문 HTML</div>")
            .imageLinks(List.of(
                "https://cdn.example.com/image1.jpg",
                "https://cdn.example.com/image2.jpg"
            ))
            .createAt(LocalDateTime.now())
            .modifiedAt(LocalDateTime.now())
            .build();

        when(adminNotificationFacade.createNotice(eq(adminId), any(AdminNotificationCreateRequest.class), anyList()))
            .thenReturn(mockResponse);

        String requestJson = objectMapper.writeValueAsString(createRequest);
        MockMultipartFile requestPart = new MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            requestJson.getBytes(StandardCharsets.UTF_8)
        );

        // when & then
        mockMvc.perform(
                MockMvcRequestBuilders.multipart(AdminApiPath.PREFIX + "/notifications/" + adminId + "/register")
                    .file(image1)
                    .file(image2)
                    .file(requestPart)
            )
            .andDo(print())
            .andExpect(status().isOk());

        // Facade 호출 검증
        verify(adminNotificationFacade).createNotice(eq(adminId), any(AdminNotificationCreateRequest.class), anyList());
    }
}