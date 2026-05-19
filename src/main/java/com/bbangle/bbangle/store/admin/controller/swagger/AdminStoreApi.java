package com.bbangle.bbangle.store.admin.controller.swagger;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreRequest;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreRequest.UpdateStoreNameRejectRequest;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.StoreDetailResponse;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.StoreSearchResult;
import com.bbangle.bbangle.store.admin.service.model.RegisteredStoreInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Admin Store", description = "(관리자) 스토어 관리 API")
public interface AdminStoreApi {

    @Operation(
        summary = "(관리자) 스토어 생성",
        description = "스토어를 생성합니다."
    )
    SingleResult<StoreDetailResponse> createStoreForAdmin(
        @Valid @RequestPart("request") AdminStoreRequest.StoreDetailRequest request,
        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    );

    @Operation(
        summary = "(관리자) 스토어 업데이트",
        description = "스토어의 상세 정보를 수정합니다."
    )
    SingleResult<StoreDetailResponse> updateStoreForAdmin(
        @Parameter(description = "수정할 스토어 번호", example = "1")
        @PathVariable Long storeId,
        @RequestBody @Valid AdminStoreRequest.StoreDetailRequest request
    );

    @Operation(
        summary = "(관리자) 스토어명 검색",
        description = "스토어명 부분일치(공백 무시)로 활성 스토어를 페이지네이션하여 조회합니다. storeName 미입력 시 전체 조회."
    )
    SingleResult<StoreSearchResult> searchStores(
        @Parameter(description = "검색할 스토어명 (선택, 빈 값이면 전체 조회)", example = "빵그리")
        @RequestParam(required = false) String storeName,
        @Parameter(description = "조회할 페이지 번호 (1부터 시작)", example = "1")
        @RequestParam(defaultValue = "1")
        @Min(1)
        int page
    );

    @Operation(
        summary = "(관리자) 등록된 스토어 목록 조회",
        description = "등록된 전체 스토어 목록을 페이지네이션으로 조회합니다."
    )
    SingleResult<BbanglePageResponse<RegisteredStoreInfo>> getRegisteredStores(
        @ParameterObject Pageable pageable
    );

    @Operation(
        summary = "(관리자) 스토어 다중 삭제",
        description = "등록된 스토어를 hard delete 방식으로 삭제합니다."
    )
    CommonResult deleteStores(
        @Parameter(description = "삭제할 스토어 ID 목록", example = "1")
        @RequestParam List<Long> storeIds
    );

    @Operation(
        summary = "(관리자) 스토어명 변경 요청 목록 조회",
        description = "승인 대기 중인 요청 목록을 조회합니다."
    )
    SingleResult<AdminStoreResponse.UpdateStoreNameRequest> getUpdateStoreNames(
        @Parameter(description = "조회할 페이지 번호", example = "1")
        @RequestParam(name = "page", defaultValue = "1")
        @Min(1)
        int page
    );

    @Operation(
        summary = "(관리자) 스토어명 변경 요청 승인",
        description = "판매자가 요청한 스토어명 변경을 승인합니다."
    )
    SingleResult<AdminStoreResponse.UpdateStoreNameApprove> approveStoreName(
        @Parameter(description = "승인할 요청 번호", example = "1")
        @PathVariable Long requestId
    );

    @Operation(
        summary = "(관리자) 스토어명 변경 요청 거부",
        description = "판매자가 요청한 스토어명 변경을 거절합니다."
    )
    SingleResult<AdminStoreResponse.UpdateStoreNameReject> rejectStoreName(
        @Parameter(description = "거절할 요청 번호", example = "1")
        @PathVariable Long requestId,
        @RequestBody @Valid UpdateStoreNameRejectRequest request
    );
}