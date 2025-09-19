package com.bbangle.bbangle.board.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "성분 카테고리")
public record DietaryTagsRequest(
    @Schema(description = "글루텐 프리 태그 여부", example = "true")
    boolean glutenFreeTag,

    @Schema(description = "고단백 태그 여부", example = "true")
    boolean highProteinTag,

    @Schema(description = "저당 태그 여부", example = "false")
    boolean sugarFreeTag,

    @Schema(description = "비건 태그 여부", example = "false")
    boolean veganTag,

    @Schema(description = "저지방 태그 여부(키토에서 변경됨)", example = "true")
    boolean ketogenicTag
) {
}