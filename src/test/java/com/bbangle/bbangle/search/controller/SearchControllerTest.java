package com.bbangle.bbangle.search.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.page.SearchCustomPage;
import com.bbangle.bbangle.search.dto.response.SearchResponse;
import com.bbangle.bbangle.search.service.SearchService;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class SearchControllerTest extends AbstractIntegrationTest {

    private static final Long MEMBER_ID = 2L;

    @MockBean
    private SearchService searchService;

    @Test
    @DisplayName("getOrderResponse 메서드는 주문 정보를 성공적으로 가져올 수 있다")
    void getOrderResponse() throws Exception {
        // Given
        FilterRequest filterRequest = FilterRequest.builder()
            .category(Category.COOKIE)
            .highProteinTag(false)
            .ketogenicTag(true)
            .sugarFreeTag(true)
            .glutenFreeTag(true)
            .veganTag(true)
            .orderAvailableToday(false)
            .maxPrice(0)
            .minPrice(3000)
            .build();

        MultiValueMap<String, String> params = getParams(filterRequest);
        params.put("sort", Collections.singletonList(SortType.RECOMMEND.name()));
        params.put("keyword", Collections.singletonList("비건"));
        params.put("cursorId", Collections.singletonList("1"));
        params.put("memberId", Collections.singletonList("1"));

        List<BoardResponseDto> boardResponseDtos = List.of(BoardResponseDto.builder()
            .boardId(1L)
            .storeId(1L)
            .storeName("Test")
            .thumbnail("Test")
            .title("Test")
            .price(0)
            .isWished(true)
            .isBundled(true)
            .tags(List.of("glutenFree"))
            .build());

        SearchResponse searchResponse = SearchResponse.of(boardResponseDtos, 0L);
        SearchCustomPage searchCustomPage = SearchCustomPage.from(searchResponse, 1L, true);

        given(searchService.getBoardList(any(), any(), any(), any(), any())).willReturn(
            searchCustomPage);

        // When & Then
        mockMvc.perform(get("/api/v1/search/boards")
                .params(params))
            .andDo(print())
            .andExpect(status().isOk());
    }

    private MultiValueMap<String, String> getParams(Object obj) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value != null) {
                    params.add(field.getName(), value.toString());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return params;
    }

    @Test
    @DisplayName("saveKeyword는 검색어를 저장할 수 있다")
    void saveKeyword() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap();
        params.put("keyword", Collections.singletonList("비건"));

        mockMvc.perform(post("/api/v1/search")
                .params(params)
                .with(user(String.valueOf(MEMBER_ID)).password("testPassword").roles("USER")))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("deleteRecencyKeyword 메서드는 최근 검색어를 삭제할 수 있다")
    void deleteRecencyKeyword() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap();
        params.put("keyword", Collections.singletonList("비건"));

        mockMvc.perform(delete("/api/v1/search/recency")
                .params(params)
                .with(user(String.valueOf(MEMBER_ID)).password("testPassword").roles("USER")))
            .andDo(print())
            .andExpect(status().isOk());
    }

    // AutoComplete 리팩토링 때 보수공사 하겠습니다
//    @Test
//    @DisplayName("getAutoKeyword 메서드는 최근 검색어를 삭제할 수 있다")
//    void getAutoKeyword() throws Exception {
//        MultiValueMap<String, String> params = new LinkedMultiValueMap();
//        params.put("keyword", Collections.singletonList("비건"));
//
//        when(searchService.getAutoKeyword(any())).thenReturn(List.of("비건", "비건 베이커리", "비건 빵"));
//
//        mockMvc.perform(delete("/api/v1/search/auto-keyword")
//                .params(params))
//            .andDo(print())
//            .andExpect(status().isOk())
//            .andReturn();
//    }
}

