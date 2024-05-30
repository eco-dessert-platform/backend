package com.bbangle.bbangle.review.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.common.domain.Badge;
import com.bbangle.bbangle.common.image.service.S3Service;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.page.ImageCustomPage;
import com.bbangle.bbangle.page.ReviewCustomPage;
import com.bbangle.bbangle.review.domain.*;
import com.bbangle.bbangle.review.dto.*;
import com.bbangle.bbangle.review.repository.ReviewImgRepository;
import com.bbangle.bbangle.review.repository.ReviewLikeRepository;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.bbangle.bbangle.exception.BbangleErrorCode.IMAGE_NOT_FOUND;
import static com.bbangle.bbangle.exception.BbangleErrorCode.REVIEW_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BoardRepository boardRepository;
    private final ReviewImgRepository reviewImgRepository;
    private final MemberRepository memberRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final S3Service s3Service;
    private static final String BOARD_FOLDER = "board/";
    private static final String REVIEW_FOLDER= "/review/";
    private static final Long PAGE_SIZE = 10L;
    private final ReviewManager reviewManager;

    @Transactional
    public void makeReview(ReviewRequest reviewRequest, Long memberId) {
        memberRepository.findMemberById(memberId);
        Review review = Review.builder()
            .content(reviewRequest.content())
            .rate(reviewRequest.rate())
            .memberId(memberId)
            .boardId(reviewRequest.boardId())
            .build();
        List<Badge> badges = reviewRequest.badges();
        badges.forEach(review::insertBadge);
        reviewRepository.save(review);
        if(Objects.isNull(reviewRequest.photos())){
            return;
        }
        makeReviewImg(reviewRequest, review);
    }

    private void makeReviewImg(ReviewRequest reviewRequest, Review review) {
        Long reviewId = review.getId();
        Long boardId = reviewRequest.boardId();
        List<MultipartFile> photos = reviewRequest.photos();
        photos.stream()
            .map(photo -> s3Service.saveImage(photo, BOARD_FOLDER + boardId + REVIEW_FOLDER + reviewId))
            .forEach(reviewImgPath -> reviewImgRepository.save(ReviewImg.builder()
                                        .reviewId(reviewId)
                                        .url(reviewImgPath)
                                        .build()));
    }

    @Transactional(readOnly = true)
    public ReviewRateResponse getReviewRate(Long boardId) {
        List<Review> reviews = reviewRepository.findByBoardId(boardId);
        return ReviewRateResponse.from(reviews);
    }

    @Transactional(readOnly = true)
    public ReviewCustomPage<List<ReviewInfoResponse>> getReviews(Long boardId,
                                                                 Long cursorId,
                                                                 Long memberId) {
        memberId = memberId != null ? memberId : 0L;
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));
        List<ReviewSingleDto> reviewSingleList = reviewRepository.getReviewSingleList(board.getId(), cursorId);
        int reviewSingListSize = reviewSingleList.size();
        boolean hasNext = reviewSingListSize >= PAGE_SIZE + 1;
        Long nextCursor = null;
        Long endCursor = null;
        if(reviewSingListSize != 0){
            nextCursor = reviewSingleList.get(reviewSingListSize -1).id();
            endCursor = reviewSingleList.get(0).id();
        }
        if (hasNext) {
            reviewSingleList.remove(reviewSingleList.get(reviewSingListSize - 1));
        }
        ReviewCursor reviewCursor = ReviewCursor.builder()
                .nextCursor(nextCursor)
                .endCursor(endCursor)
                .build();
        Map<Long, List<String>> imageMap = reviewRepository.getImageMap(reviewCursor);
        Map<Long, List<String>> tagMap = new HashMap<>();
        for(ReviewSingleDto reviewSingleDto : reviewSingleList){
            reviewManager.getTagMap(reviewSingleDto, tagMap);
        }
        Map<Long, List<Long>> likeMap = makeLikeMap(reviewCursor);
        List<ReviewInfoResponse> reviewInfoResponseList =
                ReviewInfoResponse.createList(reviewSingleList, imageMap, tagMap, likeMap, memberId);
        return ReviewCustomPage.from(reviewInfoResponseList, nextCursor, hasNext);
    }

    @Transactional
    public void insertLike(Long reviewId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOTFOUND_MEMBER));

        reviewLikeRepository.save(ReviewLike.builder()
                .memberId(member.getId())
                .reviewId(reviewId)
                .build());
    }

    @Transactional
    public void removeLike(Long reviewId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOTFOUND_MEMBER));
        reviewLikeRepository.findByMemberIdAndReviewId(member.getId(), reviewId)
                .ifPresent(reviewLikeRepository::delete);
    }

    @Transactional(readOnly = true)
    public ReviewInfoResponse getReviewDetail(Long reviewId, Long memberId) {
        ReviewSingleDto reviewDetail = reviewRepository.getReviewDetail(reviewId, memberId);
        ReviewCursor reviewCursor = ReviewCursor.builder()
                .reviewId(reviewId)
                .build();
        Map<Long, List<String>> imageMap = reviewRepository.getImageMap(reviewCursor);
        Map<Long, List<String>> tagMap = new HashMap<>();
        reviewManager.getTagMap(reviewDetail, tagMap);
        Map<Long, List<Long>> likeMap = makeLikeMap(reviewCursor);
        return ReviewInfoResponse.create(reviewDetail, imageMap, tagMap, likeMap, memberId);
    }

    private Map<Long, List<Long>> makeLikeMap(ReviewCursor reviewCursor) {
        List<ReviewLike> likeList = reviewRepository.getLikeList(reviewCursor);
        return reviewManager.getLikeMap(likeList);
    }

    @Transactional(readOnly = true)
    public ReviewImagesResponse getReviewImages(Long reviewId) {
        List<ReviewImg> reviewImgs = reviewImgRepository.findByReviewId(reviewId);
        List<String> urlList = reviewImgs.stream()
                .map(ReviewImg::getUrl)
                .toList();
        return new ReviewImagesResponse(urlList);
    }

    @Transactional(readOnly = true)
    public ReviewCustomPage<List<ReviewInfoResponse>> getMyReviews(Long memberId, Long cursorId) {
        List<ReviewSingleDto> myReviewList = reviewRepository.getMyReviews(memberId, cursorId);
        int myReviewListSize = myReviewList.size();
        boolean hasNext = myReviewListSize >= PAGE_SIZE + 1;
        Long nextCursor = null;
        Long endCursor = null;
        if(myReviewListSize != 0){
            nextCursor = myReviewList.get(myReviewListSize -1).id();
            endCursor = myReviewList.get(0).id();
        }
        if (hasNext) {
            myReviewList.remove(myReviewList.get(myReviewListSize - 1));
        }
        ReviewCursor reviewCursor = ReviewCursor.builder()
                .nextCursor(nextCursor)
                .endCursor(endCursor)
                .build();
        Map<Long, List<String>> imageMap = reviewRepository.getImageMap(reviewCursor);
        Map<Long, List<String>> tagMap = new HashMap<>();
        for(ReviewSingleDto reviewSingleDto : myReviewList){
            reviewManager.getTagMap(reviewSingleDto, tagMap);
        }
        Map<Long, List<Long>> likeMap = makeLikeMap(reviewCursor);
        List<ReviewInfoResponse> reviewInfoResponseList =
                ReviewInfoResponse.createList(myReviewList, imageMap, tagMap, likeMap, memberId);
        return ReviewCustomPage.from(reviewInfoResponseList, nextCursor, hasNext);
    }

    @Transactional(readOnly = true)
    public ImageCustomPage<List<ReviewImg>> getAllImagesByBoardId(Long boardId) {
        //Temporary
        return ImageCustomPage.from(List.of(ReviewImg.builder().id(1L).url("image1").build()), 0L, false);
    }

    @Transactional(readOnly = true)
    public ReviewImg getImage(Long imageId) {
        ReviewImg reviewImg = reviewImgRepository.findById(imageId)
                .orElseThrow(() -> new BbangleException(IMAGE_NOT_FOUND));
        return ReviewImg.builder()
                .url(reviewImg.getUrl())
                .build();
    }

    public void updateReview(ReviewRequest reviewRequest, Long reviewId, Long memberId) {
        memberRepository.findMemberById(memberId);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BbangleException(REVIEW_NOT_FOUND));
        review.update(reviewRequest);
        makeReviewImg(reviewRequest, review);
        //TODO 사진 삭제 API 필요
    }
}
