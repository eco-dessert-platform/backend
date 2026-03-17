package com.bbangle.bbangle.store.seller.controller.swagger;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.exception.GlobalControllerAdvice;
import com.bbangle.bbangle.store.seller.controller.dto.StoreApplicationRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreApplicationResponse;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Seller Store", description = "(판매자) 스토어 API")
public interface SellerStoreApi {

    @Operation(
        summary = "스토어 등록 신청",
        description = """
            ### 스토어 정보(JSON)와 프로필 이미지 파일을 업로드합니다.
            - 만약 중복 체크를 통해 조회한 스토어를 등록할 경우 JSON에 storeId가 필요합니다.
            - 새로 스토어를 등록하는 경우 storeId는 null이어야 합니다.
            - 기존 스토어 프로필 경로와 이미지 파일 둘 다 없을 경우 예외가 발생합니다.
            - 둘 중 하나는 반드시 필요합니다.
            """
    )
    SingleResult<StoreApplicationResponse.StoreApplicationDetail> registerStore(
        @AuthenticationPrincipal Long sellerId,
        @Valid @RequestPart("request") StoreApplicationRequest.StoreApplicationCreateRequest request,
        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    );

    @Operation(
        summary = "스토어 등록 신청 정보 조회",
        description = """
            ### 판매자가 신청한 스토어 등록 정보를 조회합니다.
            - 등록 신청한 스토어가 없을 경우 null을 반환합니다.
            """
    )
    SingleResult<StoreApplicationResponse.StoreApplicationDetail> getStoreApplication(
        @AuthenticationPrincipal Long sellerId
    );

    @Operation(
        summary = "(판매자) 스토어 검색",
        description = """
            ### 스토어 이름을 통해 스토어 목록을 조회
            - storeName에 스토어 이름을 입력하실 때 **빈칸을 제거**하고 입력하셔야합니다.
            - EX) `storeName.replaceAll(" " , "")`
            """
    )
    SingleResult<CursorPagination<SellerStoreInfo.StoreInfo>> search(
        @Parameter(description = "검색어", example = "빵그리의오븐")
        @NotBlank(message = "스토어 이름은 필수입니다.")
        String storeName,
        @Parameter(description = "조회한 목록의 마지막 스토어의 id", example = "1")
        Long cursorId
    );

    @Operation(
        summary = "스토어명 중복 확인",
        description = """
        ### 입력한 스토어 이름이 사용가능한지 체크하고 스토어 상세 정보를 조회

        ---
        ### 스토어 상태 설명 표
        |Status Code|의미|설명|
        |-----------|----|----|
        |RESERVED|선점|판매자가 해당 가게를 등록했지만 관리자가 승인하지 않은 상태|
        |ACTIVE|등록|관리자가 승인하여 최종적으로 해당 가게 등록자가 된 상태|
        |NONE|비선점|해당 스토어를 등록한 판매자가 아직 없는 상태|
        """
    )
    @ApiResponses(value = {
              @ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 스토어명",
            content = @Content(
                schema = @Schema(implementation = GlobalControllerAdvice.class)
            )
        )
    })
    SingleResult<StoreResponse.StoreNameCheck> checkStoreNameDuplicate(
        @Parameter(description = "스토어명", example = "빵그리의 오븐")
        @NotBlank(message = "스토어 이름은 필수입니다.")
        String storeName
    );

    @Operation(
        summary = "판매자의 스토어 정보 조회",
        description = "판매자가 등록한 스토어의 상세 정보를 조회합니다."
    )
    SingleResult<StoreResponse.SellerStoreDTO> getRegisteredStoreDetail(
        @AuthenticationPrincipal Long sellerId
    );

    @Operation(
        summary = "스토어명 변경 신청",
        description = "승인된 후에는 변경 신청 불가"
    )
    SingleResult<StoreResponse.UpdateStoreNameResponse> updateStoreName(
        @AuthenticationPrincipal Long sellerId,
        @RequestBody StoreRequest.UpdateStoreNameRequest request
    );
}
