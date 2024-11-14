package com.bbangle.bbangle.board.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.SimilarityTypeEnum;
import com.bbangle.bbangle.board.service.solution.domain.SolutionEnum;
import com.bbangle.bbangle.board.service.dto.SimilarityBoardTestDto;
import com.bbangle.bbangle.board.service.solution.SolutionResolver;
import com.bbangle.bbangle.board.service.solution.SolutionContext;
import com.bbangle.bbangle.board.service.solution.SolutionDataResolver;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BbanggreeSolutionTest extends AbstractIntegrationTest {

    @Autowired
    SolutionDataResolver solutionDataResolver;

    @Autowired
    SolutionResolver solutionResolver;

    @Test
    void solutionTest() {

        List<SimilarityBoardTestDto> similarityBoardTestDtoList = List.of(
            getSimilarityDto(
                true,
                true,
                false,
                true,
                false,
                Category.COOKIE,
                false
            ), getSimilarityDto(
                true,
                true,
                false,
                true,
                false,
                Category.COOKIE,
                false
            ));

        Map<Long, List<SimilarityBoardTestDto>> inputData = new HashMap<>();
        inputData.put(1L, similarityBoardTestDtoList);

        List<SolutionEnum> solutionEnums = List.of(
            SolutionEnum.태그,
            SolutionEnum.묶음상품,
            SolutionEnum.품절여부
        );

        SolutionContext context = SolutionContext.builder()
            .solutionEnums(solutionEnums)
            .build();

        solutionDataResolver.resolver(
            context,
            inputData
        );

        solutionResolver.resolver(context);

        context.getOutputData().forEach((boardId, enumMap) ->
            enumMap.forEach((solutionEnum, value) -> {
                switch (solutionEnum) {
                    case 태그:
                        assertThat(List.of("glutenFree", "highProtein", "vegan")).isEqualTo(value);
                        break;
                    case 묶음상품:
                        assertThat(Boolean.FALSE).isEqualTo(value);
                        break;
                    case 품절여부:
                        assertThat(Boolean.FALSE).isEqualTo(value);
                        break;
                }
            })
        );
    }

    @Test
    void solutionTest2() {

        List<SimilarityBoardTestDto> similarityBoardTestDtoList = List.of(
            getSimilarityDto(
                true,
                true,
                false,
                true,
                false,
                Category.COOKIE,
                true
            ), getSimilarityDto(
                true,
                false,
                false,
                true,
                true,
                Category.COOKIE,
                true
            ));

        Map<Long, List<SimilarityBoardTestDto>> inputData = new HashMap<>();
        inputData.put(1L, similarityBoardTestDtoList);

        List<SolutionEnum> solutionEnums = List.of(
            SolutionEnum.태그,
            SolutionEnum.묶음상품,
            SolutionEnum.품절여부
        );

        SolutionContext context = SolutionContext.builder()
            .solutionEnums(solutionEnums)
            .build();

        solutionDataResolver.resolver(
            context,
            inputData
        );

        solutionResolver.resolver(context);

        context.getOutputData().forEach((boardId, enumMap) ->
            enumMap.forEach((solutionEnum, value) -> {
                switch (solutionEnum) {
                    case 태그:
                        assertThat(
                            List.of("glutenFree", "highProtein", "vegan", "ketogenic")).isEqualTo(
                            value);
                        break;
                    case 묶음상품:
                        assertThat(Boolean.FALSE).isEqualTo(value);
                        break;
                    case 품절여부:
                        assertThat(Boolean.TRUE).isEqualTo(value);
                        break;
                }
            })
        );
    }

    @Test
    void solutionTest3() {
        List<SimilarityBoardTestDto> similarityBoardTestDtoList = List.of(
            getSimilarityDto(
                true,
                true,
                false,
                true,
                false,
                Category.COOKIE,
                true
            ));

        List<SimilarityBoardTestDto> similarityBoardTestDtoList2 = List.of(
            getSimilarityDto(
                true,
                true,
                false,
                true,
                false,
                Category.COOKIE,
                true
        ));

        Map<Long, List<SimilarityBoardTestDto>> inputData = new HashMap<>();
        inputData.put(1L, similarityBoardTestDtoList);
        inputData.put(2L, similarityBoardTestDtoList2);

        List<SolutionEnum> solutionEnums = List.of(
            SolutionEnum.태그,
            SolutionEnum.묶음상품,
            SolutionEnum.품절여부
        );

        SolutionContext context = SolutionContext.builder()
            .solutionEnums(solutionEnums)
            .build();

        solutionDataResolver.resolver(
            context,
            inputData
        );

        solutionResolver.resolver(context);

        assertThat(context.getOutputData()).hasSize(2);
        context.getOutputData().forEach((boardId, enumMap) ->
            enumMap.forEach((solutionEnum, value) -> {
                switch (solutionEnum) {
                    case 태그:
                        assertThat(
                            List.of("glutenFree", "highProtein", "vegan")).isEqualTo(
                            value);
                        break;
                    case 묶음상품:
                        assertThat(Boolean.FALSE).isEqualTo(value);
                        break;
                    case 품절여부:
                        assertThat(Boolean.TRUE).isEqualTo(value);
                        break;
                }
            })
        );
    }

    @Test
    void solutionTest4() {
        List<SimilarityBoardTestDto> similarityBoardTestDtoList = List.of(
            getSimilarityDto(
                true,
                true,
                false,
                true,
                false,
                Category.COOKIE,
                true
            ),getSimilarityDto(
                true,
                true,
                false,
                true,
                false,
                Category.BREAD,
                false
            ));

        Map<Long, List<SimilarityBoardTestDto>> inputData = new HashMap<>();
        inputData.put(1L, similarityBoardTestDtoList);

        List<SolutionEnum> solutionEnums = List.of(
            SolutionEnum.태그,
            SolutionEnum.묶음상품,
            SolutionEnum.품절여부
        );

        SolutionContext context = SolutionContext.builder()
            .solutionEnums(solutionEnums)
            .build();

        solutionDataResolver.resolver(
            context,
            inputData
        );

        solutionResolver.resolver(context);

        context.getOutputData().forEach((boardId, enumMap) ->
            enumMap.forEach((solutionEnum, value) -> {
                switch (solutionEnum) {
                    case 태그:
                        assertThat(
                            List.of("glutenFree", "highProtein", "vegan")).isEqualTo(
                            value);
                        break;
                    case 묶음상품:
                        assertThat(Boolean.TRUE).isEqualTo(value);
                        break;
                    case 품절여부:
                        assertThat(Boolean.FALSE).isEqualTo(value);
                        break;
                }
            })
        );
    }

    private SimilarityBoardTestDto getSimilarityDto(
        Boolean glutenFreeTag,
        Boolean highProteinTag,
        Boolean sugarFreeTag,
        Boolean veganTag,
        Boolean ketogenicTag,
        Category category,
        Boolean isSoldOut
    ) {
        return new SimilarityBoardTestDto(
            2L,                     // boardId
            101L,                   // storeId
            "https://example.com/thumbnail.jpg", // thumbnail
            "Healthy Store",        // storeName
            "Organic Protein Bar",  // title
            20,                     // discountRate (20%)
            1500,                   // price (in cents or lowest unit of currency)
            new BigDecimal("4.5"),  // reviewRate (4.5 stars)
            250L,                   // reviewCount
            glutenFreeTag,
            highProteinTag,
            sugarFreeTag,
            veganTag,
            ketogenicTag,
            isSoldOut,
            category,
            SimilarityTypeEnum.DEFAULT, // similarityType
            true // isWished
        );
    }
}
