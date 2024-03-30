package com.bbangle.bbangle.controller;

import com.bbangle.bbangle.config.CorsConfig;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.config.WebOAuthSecurityConfig;
import com.bbangle.bbangle.configuration.TestSecurityConfig;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.wishListFolder.repository.WishListFolderRepository;
import com.bbangle.bbangle.wishListFolder.repository.WishListFolderRepositoryImpl;
import com.bbangle.bbangle.wishListStore.controller.WishListStoreController;
import com.bbangle.bbangle.wishListStore.repository.WishListStoreRepositoryImpl;
import com.bbangle.bbangle.wishListStore.service.WishListStoreServiceImpl;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*@AutoConfigureMockMvc*/
@Import({TestSecurityConfig.class})
@WebMvcTest(value = WishListStoreController.class,
            excludeFilters = {
                    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                            classes = {WebOAuthSecurityConfig.class, CorsConfig.class})
            })
public class WishListStoreControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    WishListStoreServiceImpl wishListStoreService;

    @MockBean
    MemberRepository memberRepository;

    @MockBean
    WishListFolderRepository wishListFolderRepository;

    @MockBean
    WishListFolderRepositoryImpl WishListFolderRepositoryImpl;

    @MockBean
    WishListStoreRepositoryImpl wishListStoreRepository;

    private static final Authentication AUTHENTICATION = new TestingAuthenticationToken(
            "test1@gmail.com",
            null,
            "ROLE_USER");

    @BeforeEach
    public void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new WishListStoreController(wishListStoreService)).build();
    }

    private final String BEARER = "Bearer";
    private final String AUTHORIZATION = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJiYmFuZ2xlYmJhbmdsZSIsImlhdCI6MTcxMDU5NDgzMSwiZXhwIjoxNzEwNjA1NjMxLCJzdWIiOiJkc3lvb24xOTk0QGdtYWlsLmNvbSIsImlkIjoxMn0.EdRBBy5Orzlv_oZm4hGRZQZZ79_H-1JJHjzXZxlM_YU";

    @DisplayName("위시리스트 스토어 전체 조회를 시행한다 오프셋 기반 페이지 네이션")
    @Test
    public void getWishListStores() throws Exception{
        mockMvc.perform(get("/api/v1/likes/stores")
                .header("Authorization", String.format("%s %s",BEARER, AUTHORIZATION))
                .with(SecurityMockMvcRequestPostProcessors.authentication(AUTHENTICATION))
                .param("page", "0")
                .param("size", "1")
                .param("sort", "createdAt,DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contents[0].introduce").value("건강을 먹다_로썸"))
                .andDo(print());
    }

    @DisplayName("위시리스트 스토어 전체 조회를 시행한다 커서 기반 페이지 네이션")
    @Test
    public void getWishListStoresByCursor() throws Exception{
        mockMvc.perform(get("/api/v1/likes/stores/cursor")
                        .header("Authorization", String.format("%s %s",BEARER, AUTHORIZATION))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(AUTHENTICATION))
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contents[0].introduce").value("건강을 먹다_로썸"))
                .andDo(print());
    }
}
