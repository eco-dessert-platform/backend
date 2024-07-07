package com.bbangle.bbangle.search.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.page.SearchCustomPage;
import com.bbangle.bbangle.search.dto.response.SearchResponse;
import com.bbangle.bbangle.search.service.SearchService;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class SearchControllerTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @MockBean
    private ResponseService responseService;

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

        SingleResult singleResult = new SingleResult<>();
        singleResult.setResult(searchCustomPage);
        singleResult.setSuccess(true);

        given(searchService.getBoardList(any(), any(), any(), any(), any())).willReturn(searchCustomPage);
        given(responseService.getSingleResult(any())).willReturn(singleResult);

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
}

