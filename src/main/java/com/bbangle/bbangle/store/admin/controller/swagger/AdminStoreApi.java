package com.bbangle.bbangle.store.admin.controller.swagger;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Admin Store", description = "(관리자) 스토어 관리 API")
public interface AdminStoreApi {

    @Operation(
        summary = "(관리자) 스토어명 변경 요청 목록 조회",
        description = "승인 대기 중인 요청 목록을 조회합니다."
    )
    SingleResult<AdminStoreResponse.UpdateStoreNameRequest> getUpdateStoreNames(
        @Parameter(description = "조회할 페이지 번호", example = "1")
        @RequestParam(defaultValue = "1")
        @Min(1)
        int page
    );
}