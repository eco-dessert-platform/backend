package com.bbangle.bbangle.review.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.review.dto.ReviewImageUploadRequest;
import com.bbangle.bbangle.review.dto.ReviewImageUploadResponse;
import com.bbangle.bbangle.review.dto.ReviewRequest;
import com.bbangle.bbangle.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/review")
@Tag(name = "Reviews", description = "리뷰 API")
public class ReviewController {

    private final ReviewService reviewService;
    private final ResponseService responseService;

    @Operation(summary = "리뷰 작성")
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    public CommonResult createReview(
        @Valid @RequestBody
        ReviewRequest reviewRequest,
        @AuthenticationPrincipal
        Long memberId
    ){
        reviewService.makeReview(reviewRequest, memberId);
        return responseService.getSuccessResult();
    }

    @Operation(summary = "리뷰 이미지 업로드")
    @PostMapping(
            value = "/image",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public CommonResult uploadReviewImage(
            @ModelAttribute
            ReviewImageUploadRequest reviewImageUploadRequest,
            @AuthenticationPrincipal
            Long memberId
    ){
        ReviewImageUploadResponse reviewImageUploadResponse =
                reviewService.uploadReviewImage(reviewImageUploadRequest, memberId);
        return responseService.getSingleResult(reviewImageUploadResponse);
    }

    @Operation(summary = "리뷰 수정")
    @PutMapping(
            value="/{reviewId}",
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    public CommonResult updateReview(
            @PathVariable("reviewId")
            Long reviewId,
            @Valid @RequestBody
            ReviewRequest reviewRequest,
            @AuthenticationPrincipal
            Long memberId
    ){
        reviewService.updateReview(reviewRequest,reviewId, memberId);
        return responseService.getSuccessResult();
    }

    @Operation(summary = "리뷰 삭제")
    @DeleteMapping("/{reviewId}")
    public CommonResult deleteReview(
            @PathVariable
            Long reviewId,
            @AuthenticationPrincipal
            Long memberId){
        reviewService.deleteReview(reviewId, memberId);
        return responseService.getSuccessResult();
    }

    @Operation(summary = "상품페이지(리뷰) 평점")
    @GetMapping(value = "/rate/{boardId}")
    public CommonResult getReviewRate(@PathVariable("boardId") Long boardId) {
        return responseService.getSingleResult(reviewService.getReviewRate(boardId));
    }

    @Operation(summary = "상품페이지(리뷰) 목록")
    @GetMapping(value = "/list/{boardId}")
    public CommonResult getReviews(
    @PathVariable("boardId")
    Long boardId,
    @RequestParam(required = false, value = "cursorId")
    Long cursorId,
    @AuthenticationPrincipal
    Long memberId) {
        return responseService.getSingleResult(reviewService.getReviews(boardId, cursorId, memberId));
    }

    @Operation(summary = "도움되요 추가")
    @GetMapping(value = "/like/{reviewId}")
    public CommonResult insertLike(
            @PathVariable("reviewId")
            Long reviewId,
            @AuthenticationPrincipal
            Long memberId){
        reviewService.insertLike(reviewId,memberId);
        return responseService.getSuccessResult();
    }

    @Operation(summary = "도움되요 해제")
    @DeleteMapping(value = "/like/{reviewId}")
    public CommonResult removeLike(
            @PathVariable("reviewId")
            Long reviewId,
            @AuthenticationPrincipal
            Long memberId){
        reviewService.removeLike(reviewId,memberId);
        return responseService.getSuccessResult();
    }

    @Operation(summary = "리뷰 상세")
    @GetMapping(value = "/{reviewId}")
    public CommonResult getReviewDetail(
            @PathVariable("reviewId")
            Long reviewId,
            @AuthenticationPrincipal
            Long memberId ){
        return responseService.getSingleResult(reviewService.getReviewDetail(reviewId,memberId));
    }

    @Operation(summary = "리뷰 대표 이미지")
    @GetMapping(value = "/images/{reviewId}")
    public CommonResult getReviewImages(@PathVariable("reviewId") Long reviewId){
        return responseService.getSingleResult(reviewService.getReviewImages(reviewId));
    }

    @Operation(summary = "내가 작성한 리뷰 조회")
    @GetMapping(value = "/myreview")
    public CommonResult getMyReviews(
            @AuthenticationPrincipal
            Long memberId,
            @RequestParam(required = false, value = "cursorId")
            Long cursorId){
        return responseService.getSingleResult(reviewService.getMyReviews(memberId,cursorId));
    }

    @Operation(summary = "사진 리뷰 전체보기")
    @GetMapping(value = "/{boardId}/images")
    public CommonResult getAllImagesByBoardId(
            @PathVariable("boardId")
            Long boardId,
            @RequestParam(required = false, value = "cursorId")
            Long cursorId){
        return responseService.getSingleResult(reviewService.getAllImagesByBoardId(boardId, cursorId));
    }

    @Operation(summary = "사진 리뷰 크게보기")
    @GetMapping(value = "/image/{imageId}")
    public CommonResult getImage(@PathVariable("imageId") Long imageId){
        return responseService.getSingleResult(reviewService.getImage(imageId));
    }

    @Operation(summary = "리뷰 수정 화면에서 사진 삭제")
    @DeleteMapping(value = "/image/{imageId}")
    public CommonResult deleteImage(@PathVariable("imageId") Long imageId){
        reviewService.deleteImage(imageId);
        return responseService.getSuccessResult();
    }
}
