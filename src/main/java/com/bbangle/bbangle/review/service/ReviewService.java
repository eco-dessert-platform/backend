package com.bbangle.bbangle.review.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.page.ImageCustomPage;
import com.bbangle.bbangle.page.ReviewCustomPage;
import com.bbangle.bbangle.boardstatistic.service.RankingService;
import com.bbangle.bbangle.review.domain.Badge;
import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.review.domain.ReviewCursor;
import com.bbangle.bbangle.review.domain.ReviewImg;
import com.bbangle.bbangle.review.domain.ReviewLike;
import com.bbangle.bbangle.review.domain.ReviewManager;
import com.bbangle.bbangle.review.dto.ReviewImagesResponse;
import com.bbangle.bbangle.review.dto.ReviewImgDto;
import com.bbangle.bbangle.review.dto.ReviewInfoResponse;
import com.bbangle.bbangle.review.dto.ReviewRateResponse;
import com.bbangle.bbangle.review.dto.ReviewRequest;
import com.bbangle.bbangle.review.dto.ReviewSingleDto;
import com.bbangle.bbangle.review.repository.ReviewImgRepository;
import com.bbangle.bbangle.review.repository.ReviewLikeRepository;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bbangle.bbangle.exception.BbangleErrorCode.IMAGE_NOT_FOUND;
import static com.bbangle.bbangle.exception.BbangleErrorCode.REVIEW_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private static final Double REVIEW_SCORE= 50.0;

    private final RankingService rankingService;
    private static final Long PAGE_SIZE = 10L;
    private static final Long NON_MEMBER = 0L;

    private final ReviewRepository reviewRepository;
    private final BoardRepository boardRepository;
    private final ReviewImgRepository reviewImgRepository;
    private final MemberRepository memberRepository;
    private final ReviewLikeRepository reviewLikeRepository;
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
        rankingService.updateRankingScore(reviewRequest.boardId(), REVIEW_SCORE);

   /*     //FIXME 따로 구현!
        if(Objects.isNull(reviewRequest.photos())){
            return;
        }
        makeReviewImg(reviewRequest, review);*/
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
        memberId = memberId != null ? memberId : NON_MEMBER;
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));
        List<ReviewSingleDto> reviewSingleList = reviewRepository.getReviewSingleList(board.getId(), cursorId);
        int reviewSingListSize = reviewSingleList.size();
        Long nextCursor = null;
        Long lastCursor = null;
        if(reviewSingListSize != 0){
            nextCursor = reviewSingleList.get(reviewSingListSize -1).id();
            lastCursor = reviewSingleList.stream()
                    .findFirst()
                    .get()
                    .id();
        }
        ReviewCursor reviewCursor = ReviewCursor.builder()
                .nextCursor(nextCursor)
                .lastCursor(lastCursor)
                .build();
        boolean hasNext = checkHasNext(reviewSingListSize);
        if (hasNext) {
            reviewSingleList.remove(reviewSingleList.get(reviewSingListSize - 1));
        }
        Map<Long, List<ReviewImgDto>> imageMap = reviewRepository.getImageMap(reviewCursor);
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
        memberId = memberId == null ? NON_MEMBER : memberId;
        ReviewSingleDto reviewDetail = reviewRepository.getReviewDetail(reviewId);
        ReviewCursor reviewCursor = ReviewCursor.builder()
                .reviewId(reviewId)
                .build();
        Map<Long, List<ReviewImgDto>> imageMap = reviewRepository.getImageMap(reviewCursor);
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
        Long nextCursor = null;
        Long lastCursor = null;
        if(myReviewListSize != 0){
            nextCursor = myReviewList.get(myReviewListSize -1).id();
            lastCursor = myReviewList.stream().findFirst().get().id();
        }
        ReviewCursor reviewCursor = ReviewCursor.builder()
                .nextCursor(nextCursor)
                .lastCursor(lastCursor)
                .build();
        boolean hasNext = checkHasNext(myReviewListSize);
        if (hasNext) {
            myReviewList.remove(myReviewList.get(myReviewListSize - 1));
        }
        Map<Long, List<ReviewImgDto>> imageMap = reviewRepository.getImageMap(reviewCursor);
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
    public ImageCustomPage<List<ReviewImgDto>> getAllImagesByBoardId(Long boardId, Long cursorId) {
        List<ReviewImgDto> allImagesByBoardId = reviewRepository.getAllImagesByBoardId(boardId, cursorId);
        int allImagesSize = allImagesByBoardId.size();
        boolean hasNext = checkHasNext(allImagesSize);
        Long nextCursor = null;
        if(allImagesSize != 0){
            nextCursor = allImagesByBoardId.get(allImagesSize -1).getId();
        }
        if (hasNext) {
            allImagesByBoardId.remove(allImagesByBoardId.get(allImagesSize - 1));
        }
        return ImageCustomPage.from(allImagesByBoardId, nextCursor, hasNext);
    }

    @Transactional(readOnly = true)
    public ReviewImgDto getImage(Long imageId) {
        ReviewImg reviewImg = reviewImgRepository.findById(imageId)
                .orElseThrow(() -> new BbangleException(IMAGE_NOT_FOUND));
        return ReviewImgDto.builder()
                .url(reviewImg.getUrl())
                .build();
    }
    @Transactional
    public void updateReview(ReviewRequest reviewRequest, Long reviewId, Long memberId) {
        memberRepository.findMemberById(memberId);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BbangleException(REVIEW_NOT_FOUND));
        review.update(reviewRequest);
        makeReviewImg(reviewRequest, review);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long memberId) {
        memberRepository.findMemberById(memberId);
        List<ReviewImg> reviewImgs = reviewImgRepository.findByReviewId(reviewId);
        List<ReviewLike> reviewLikes = reviewLikeRepository.findByReviewId(reviewId);
        if (!reviewImgs.isEmpty()) {
            reviewImgRepository.deleteAllInBatch(reviewImgs);
        }
        if(!reviewLikes.isEmpty()) {
            reviewLikeRepository.deleteAllInBatch(reviewLikes);
        }
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BbangleException(REVIEW_NOT_FOUND));
        review.delete();
    }

    @Transactional
    public void deleteImage(Long imageId) {
        ReviewImg reviewImg = reviewImgRepository.findById(imageId)
                .orElseThrow(() -> new BbangleException(IMAGE_NOT_FOUND));
        reviewImgRepository.delete(reviewImg);
    }

    private void makeReviewImg(ReviewRequest reviewRequest, Review review) {
//        Long reviewId = review.getId();
//        Long boardId = reviewRequest.boardId();
//        List<String> photos = reviewRequest.photos();
//        photos.stream()
//                .map(photo -> s3Service.saveImage(photo, BOARD_FOLDER + boardId + REVIEW_FOLDER + reviewId))
//                .forEach(reviewImgPath -> reviewImgRepository.save(ReviewImg.builder()
//                        .reviewId(reviewId)
//                        .url(reviewImgPath)
//                        .build()));
    }

    private boolean checkHasNext(int size) {
        return size >= PAGE_SIZE + 1;
    }
}
