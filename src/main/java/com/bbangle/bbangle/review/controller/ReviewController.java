package com.bbangle.bbangle.review.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.ImageCustomPage;
import com.bbangle.bbangle.common.page.ReviewCustomPage;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.image.dto.ImageDto;
import com.bbangle.bbangle.review.dto.ReviewImageUploadRequest;
import com.bbangle.bbangle.review.dto.ReviewImageUploadResponse;
import com.bbangle.bbangle.review.dto.ReviewImagesResponse;
import com.bbangle.bbangle.review.dto.ReviewInfoResponse;
import com.bbangle.bbangle.review.dto.ReviewRateResponse;
import com.bbangle.bbangle.review.dto.ReviewRequest;
import com.bbangle.bbangle.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/review")
@Tag(name = "Reviews", description = "리뷰 API")
@RequiredArgsConstructor
public class ReviewController {

    private final ResponseService responseService;
    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성")
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    public CommonResult createReview(
            @Valid @RequestBody
            ReviewRequest reviewRequest,
            @AuthenticationPrincipal
            Long memberId
    ) {
        reviewService.makeReview(reviewRequest, memberId);
        return responseService.getSuccessResult();
    }

    @Operation(summary = "리뷰 이미지 업로드")
    @PostMapping(
            value = "/image",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public SingleResult<ReviewImageUploadResponse> uploadReviewImage(
            @ModelAttribute
            ReviewImageUploadRequest reviewImageUploadRequest,
            @AuthenticationPrincipal
            Long memberId
    ) {
        return responseService.getSingleResult(reviewService.uploadReviewImage(reviewImageUploadRequest, memberId));
    }

    @Operation(summary = "리뷰 수정")
    @PutMapping(
            value = "/{reviewId}",
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    public CommonResult updateReview(
            @PathVariable("reviewId") @Parameter(description = "리뷰 ID", example = "1")
            Long reviewId,
            @Valid @RequestBody
            ReviewRequest reviewRequest,
            @AuthenticationPrincipal
            Long memberId
    ) {
        reviewService.updateReview(reviewRequest, reviewId, memberId);
        return responseService.getSuccessResult();
    }

    @Operation(summary = "리뷰 삭제")
    @DeleteMapping("/{reviewId}")
    public CommonResult deleteReview(
            @PathVariable("reviewId") @Parameter(description = "리뷰 ID", example = "1")
            Long reviewId,
            @AuthenticationPrincipal
            Long memberId) {
        reviewService.deleteReview(reviewId, memberId);
        return responseService.getSuccessResult();
    }

    @Operation(summary = "상품페이지(리뷰) 평점")
    @GetMapping(value = "/rate/{boardId}")
    public SingleResult<ReviewRateResponse> getReviewRate(
            @PathVariable("boardId") @Parameter(description = "게시글 ID", example = "1") Long boardId
    ) {
        return responseService.getSingleResult(reviewService.getReviewRate(boardId));
    }

    @Operation(summary = "상품페이지(리뷰) 목록")
    @GetMapping(value = "/list/{boardId}")
    public SingleResult<ReviewCustomPage<List<ReviewInfoResponse>>> getReviews(
            @PathVariable("boardId") @Parameter(description = "게시글 ID", example = "1")
            Long boardId,
            @RequestParam(required = false, value = "cursorId") @Parameter(description = "커서 ID")
            Long cursorId,
            @AuthenticationPrincipal
            Long memberId) {
        return responseService.getSingleResult(reviewService.getReviews(boardId, cursorId, memberId));
    }

    @Operation(summary = "도움되요 추가")
    @PostMapping(value = "/like/{reviewId}")
    public CommonResult insertLike(
            @PathVariable("reviewId") @Parameter(description = "리뷰 ID", example = "1")
            Long reviewId,
            @AuthenticationPrincipal
            Long memberId) {
        reviewService.insertLike(reviewId, memberId);
        return responseService.getSuccessResult();
    }

    @Operation(summary = "도움되요 해제")
    @DeleteMapping(value = "/like/{reviewId}")
    public CommonResult removeLike(
            @PathVariable("reviewId") @Parameter(description = "리뷰 ID", example = "1")
            Long reviewId,
            @AuthenticationPrincipal
            Long memberId) {
        reviewService.removeLike(reviewId, memberId);
        return responseService.getSuccessResult();
    }

    @Operation(summary = "리뷰 상세")
    @GetMapping(value = "/{reviewId}")
    public SingleResult<ReviewInfoResponse> getReviewDetail(
            @PathVariable("reviewId") @Parameter(description = "리뷰 ID", example = "1")
            Long reviewId,
            @AuthenticationPrincipal
            Long memberId) {
        return responseService.getSingleResult(reviewService.getReviewDetail(reviewId, memberId));
    }

    @Operation(summary = "리뷰 대표 이미지")
    @GetMapping(value = "/images/{reviewId}")
    public SingleResult<ReviewImagesResponse> getReviewImages(
            @PathVariable("reviewId") @Parameter(description = "리뷰 ID", example = "1")
            Long reviewId
    ) {
        return responseService.getSingleResult(reviewService.getReviewImages(reviewId));
    }

    @Operation(summary = "내가 작성한 리뷰 조회")
    @GetMapping(value = "/myreview")
    public SingleResult<ReviewCustomPage<List<ReviewInfoResponse>>> getMyReviews(
            @AuthenticationPrincipal
            Long memberId,
            @RequestParam(required = false, value = "cursorId") @Parameter(description = "커서 ID")
            Long cursorId) {
        return responseService.getSingleResult(reviewService.getMyReviews(memberId, cursorId));
    }

    @Operation(summary = "사진 리뷰 전체보기")
    @GetMapping(value = "/{boardId}/images")
    public SingleResult<ImageCustomPage<List<ImageDto>>> getAllImagesByBoardId(
            @PathVariable("boardId") @Parameter(description = "게시글 ID", example = "1")
            Long boardId,
            @RequestParam(required = false, value = "cursorId") @Parameter(description = "커서 ID")
            Long cursorId) {
        return responseService.getSingleResult(reviewService.getAllImagesByBoardId(boardId, cursorId));
    }

    @Operation(summary = "사진 리뷰 크게보기")
    @GetMapping(value = "/image/{imageId}")
    public SingleResult<ImageDto> getImage(
            @PathVariable("imageId") @Parameter(description = "이미지 ID", example = "1")
            Long imageId) {
        return responseService.getSingleResult(reviewService.getImage(imageId));
    }

    @Operation(summary = "리뷰 수정 화면에서 사진 삭제")
    @DeleteMapping(value = "/image/{imageId}")
    public CommonResult deleteImage(
            @PathVariable("imageId") @Parameter(description = "이미지 ID", example = "1")
            Long imageId) {
        reviewService.deleteImage(imageId);
        return responseService.getSuccessResult();
    }
}
