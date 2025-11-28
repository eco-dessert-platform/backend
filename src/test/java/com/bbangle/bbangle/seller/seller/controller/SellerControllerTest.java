package com.bbangle.bbangle.seller.seller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest;
import com.bbangle.bbangle.seller.seller.facade.SellerFacade;
import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.token.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.token.jwt.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
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

@DisplayName("[컨트롤러 테스트] SellerController")
@WebMvcTest(controllers = SellerController.class)
@Import({
    TestSlackAdaptorConfig.class,
    JsonDataEncoder.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class
})
@ActiveProfiles("test")
public class SellerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SellerFacade sellerFacade;

    @MockBean
    private SellerService sellerService;


    @Test
    @WithMockUser
    @DisplayName("신규 판매자를 정상적으로 생성한다")
        // -> 테스트 결과 리포트에 표시될 이름입니다.
    void createSeller_Succeeds_WithValidInput() throws Exception {
        // given (테스트 준비 단계)

        // 1. 요청 바디에 들어갈 DTO 객체를 생성합니다.
        SellerRequest.SellerCreateRequest createRequest = new SellerRequest.SellerCreateRequest(
            "빵그리의 오븐 1호점",
            "01012345678",
            "01012345678",
            "user@example.com",
            "(우편번호) 성남시 금광동 222-31",
            "나동 202호",
            1L
        );

        // 2. 업로드할 가짜 이미지 파일을 생성합니다.
        // 파라미터 순서: (필드명, 원본파일명, 컨텐츠타입, 파일내용byte)
        MockMultipartFile profileImage = new MockMultipartFile(
            "profileImage",            // Controller에서 @RequestPart("profileImage")로 받을 이름
            "profile.jpg",             // 업로드될 파일의 이름
            MediaType.IMAGE_JPEG_VALUE,// 파일의 MIME 타입 (image/jpeg)
            "profile image content".getBytes() // 파일의 내용 (테스트용 더미 데이터)
        );

        // 3. DTO 객체를 JSON 문자열로 변환(직렬화)합니다.
        String requestJson = objectMapper.writeValueAsString(createRequest);

        // 4. JSON 데이터를 마치 파일인 것처럼 MockMultipartFile로 감쌉니다.
        // -> Multipart 요청에서 파일과 JSON을 같이 보낼 때 사용하는 기법입니다.
        MockMultipartFile requestPart = new MockMultipartFile(
            "request",                 // Controller에서 @RequestPart("request")로 받을 이름
            "",                        // JSON 데이터는 파일명이 필요 없으므로 빈 문자열
            MediaType.APPLICATION_JSON_VALUE, // 컨텐츠 타입을 application/json으로 명시
            requestJson.getBytes(StandardCharsets.UTF_8) // JSON 문자열을 바이트로 변환
        );

        // 5. Mock 객체인 sellerFacade의 동작을 정의(Stubbing)합니다.
        // -> registerSeller 메서드가 호출되면 아무런 동작도 하지 않고(에러 없이) 넘어가도록 설정합니다.
        doNothing().when(sellerFacade).registerSeller(any(), any(), any());


        // when & then (테스트 실행 및 검증 단계)
        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/api/v1/seller/sellers") // -> multipart 요청 빌더 시작
                    .file(profileImage) // -> 위에서 만든 이미지 파일 추가
                    .file(requestPart)  // -> 위에서 만든 JSON 데이터 파트 추가
            )
            .andDo(print()) // -> 요청/응답 전체 내용을 콘솔에 출력 (디버깅용)
            .andExpect(status().isOk()) // -> 응답 상태 코드가 200 OK인지 검증
            // -> 응답 JSON의 'success' 필드가 true인지 검증 (공통 응답 객체 구조로 추정됨)
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("스토어명이 비어있으면 판매자 생성에 실패한다")
    void createSeller_Fails_WithBlankStoreName() throws Exception {
        // given
        SellerRequest.SellerCreateRequest createRequest = new SellerRequest.SellerCreateRequest(
            "", // Invalid store name
            "01012345678",
            "01012345678",
            "user@example.com",
            "(우편번호) 성남시 금광동 222-31",
            "나동 202호",
            1L
        );

        MockMultipartFile profileImage = new MockMultipartFile(
            "profileImage",
            "profile.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "profile image content".getBytes()
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
                MockMvcRequestBuilders.multipart("/api/v1/seller/sellers")
                    .file(profileImage)
                    .file(requestPart)
            )
            .andDo(print())
            .andExpect(status().isBadRequest());
    }
}
