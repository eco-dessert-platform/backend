package com.bbangle.bbangle.board.seller.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "성분 카테고리")
public class DietaryTagsRequest {

    @Schema(description = "글루텐 프리 태그 여부", example = "true")
    private boolean glutenFreeTag;

    @Schema(description = "고단백 태그 여부", example = "true")
    private boolean highProteinTag;

    @Schema(description = "저당 태그 여부", example = "false")
    private boolean sugarFreeTag;

    @Schema(description = "비건 태그 여부", example = "false")
    private boolean veganTag;

    @Schema(description = "저지방 태그 여부(키토에서 변경됨)", example = "true")
    private boolean ketogenicTag;
}
