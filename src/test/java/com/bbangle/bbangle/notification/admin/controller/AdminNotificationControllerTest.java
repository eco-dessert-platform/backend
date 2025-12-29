package com.bbangle.bbangle.notification.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.config.security.AdminApiPath;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationCreateRequest;
import com.bbangle.bbangle.notification.admin.controller.dto.LinkDto;
import com.bbangle.bbangle.notification.admin.facade.AdminNotificationFacade;
import com.bbangle.bbangle.notification.admin.service.AdminNotificationService;
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
    ResponseService.class
})
@ActiveProfiles("test")
public class AdminNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminNotificationFacade adminNotificationFacade;

    @MockBean
    private AdminNotificationService adminNotificationService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("관리자 공지사항을 정상적으로 생성한다")
    void registerNotification_Succeeds_WithValidInput() throws Exception {
        // given
        Long adminId = 1L;

        // 1. 요청 DTO 생성
        List<LinkDto> links = List.of(
            new LinkDto("https://example.com", "더보기")
        );

        AdminNotificationCreateRequest createRequest = new AdminNotificationCreateRequest(
            "공지사항 제목",
            "<div>공지사항 본문 HTML</div>",
            links
        );

        // 2. 업로드할 가짜 이미지 파일들 생성
        MockMultipartFile image1 = new MockMultipartFile(
            "profileImage",
            "image1.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "image1 content".getBytes()
        );

        MockMultipartFile image2 = new MockMultipartFile(
            "profileImage",
            "image2.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "image2 content".getBytes()
        );

        // 3. DTO를 JSON으로 변환
        String requestJson = objectMapper.writeValueAsString(createRequest);

        // 4. JSON 데이터를 MockMultipartFile로 감싸기
        MockMultipartFile requestPart = new MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            requestJson.getBytes(StandardCharsets.UTF_8)
        );

        // 5. Mock 응답 데이터 생성
        NoticeInfo mockNoticeInfo = NoticeInfo.builder()
            .id(1L)
            .title("공지사항 제목")
            .content("<div>공지사항 본문 HTML</div>")
            .links(links)
            .imageLinks(List.of(
                "https://cdn.example.com/image1.jpg",
                "https://cdn.example.com/image2.jpg"
            ))
            .createAt(LocalDateTime.now())
            .modifiedAt(LocalDateTime.now())
            .build();

        // 6. Facade Mock 동작 정의
        when(adminNotificationFacade.createNotice(any(Long.class), any(), any()))
            .thenReturn(mockNoticeInfo);

        // when & then
        mockMvc.perform(
                MockMvcRequestBuilders.multipart(AdminApiPath.PREFIX + "/notifications/" + adminId + "/register")
                    .file(image1)
                    .file(image2)
                    .file(requestPart)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.id").value(1L))
            .andExpect(jsonPath("$.result.title").value("공지사항 제목"))
            .andExpect(jsonPath("$.result.content").value("<div>공지사항 본문 HTML</div>"))
            .andExpect(jsonPath("$.result.links[0].url").value("https://example.com"))
            .andExpect(jsonPath("$.result.links[0].linkText").value("더보기"))
            .andExpect(jsonPath("$.result.imageLinks[0]").value("https://cdn.example.com/image1.jpg"))
            .andExpect(jsonPath("$.result.imageLinks[1]").value("https://cdn.example.com/image2.jpg"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("제목이 비어있으면 공지사항 생성에 실패한다")
    void registerNotification_Fails_WithBlankTitle() throws Exception {
        // given
        Long adminId = 1L;

        AdminNotificationCreateRequest createRequest = new AdminNotificationCreateRequest(
            "", // 빈 제목
            "<div>공지사항 본문 HTML</div>",
            List.of()
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
            "", // 빈 본문
            List.of()
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
}