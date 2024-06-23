package com.bbangle.bbangle.review.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.boardstatistic.service.BoardStatisticService;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.domain.Image;
import com.bbangle.bbangle.image.domain.ImageCategory;
import com.bbangle.bbangle.image.repository.ImageRepository;
import com.bbangle.bbangle.image.service.ImageService;
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

import java.math.BigDecimal;
import java.util.*;

import static com.bbangle.bbangle.exception.BbangleErrorCode.IMAGE_NOT_FOUND;
import static com.bbangle.bbangle.exception.BbangleErrorCode.REVIEW_NOT_FOUND;
import static java.util.Locale.ROOT;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final Boolean WRITE = true;
    private static final Boolean DELETE = false;
    private static final String BUCKET_DOMAIN = "https://bbangle-bucket.kr.object.ncloudstorage.com/";
    private static final String CTN_DOMAIN = "https://bbangree-oven.cdn.ntruss.com/";

    private static final Long PAGE_SIZE = 10L;
    private static final Long NON_MEMBER = 0L;

    private final BoardStatisticService boardStatisticService;
    private final ReviewRepository reviewRepository;
    private final BoardRepository boardRepository;
    private final ReviewImgRepository reviewImgRepository;
    private final MemberRepository memberRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewManager reviewManager;
    private final ReviewStatistics reviewStatistics;
    private final ImageService imageService;
    private final ImageRepository imageRepository;

    @Transactional
    public void makeReview(ReviewRequest reviewRequest, Long memberId) {
        memberRepository.findMemberById(memberId);
        Review review = Review.builder()
            .content(reviewRequest.content())
            .rate(reviewRequest.rate())
            .memberId(memberId)
            .boardId(reviewRequest.boardId())
            .isBest(false)
            .build();
        List<Badge> badges = reviewRequest.badges();
        badges.forEach(review::insertBadge);
        Review savedReview = reviewRepository.save(review);
        Long reviewId = savedReview.getId();
        boardStatisticService.updateReviewCount(reviewRequest.boardId(), review.getRate(), WRITE);
        List<String> urls = reviewRequest.urls();
        if(Objects.isNull(urls)){
            return;
        }
        moveImages(urls, reviewId);
    }

    @Transactional
    public ReviewImageUploadResponse uploadReviewImage(ReviewImageUploadRequest reviewImageUploadRequest, Long memberId) {
        memberRepository.findMemberById(memberId);
        List<String> urls = imageService.saveAll(reviewImageUploadRequest.category(), reviewImageUploadRequest.images());
        return new ReviewImageUploadResponse(urls);
    }

    @Transactional(readOnly = true)
    public ReviewRateResponse getReviewRate(Long boardId) {
        List<ReviewDto> reviews = reviewRepository.findByBoardId(boardId);
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
        List<ReviewImg> reviewImages = reviewImgRepository.findByReviewId(reviewId);
        List<String> urlList = reviewImages.stream()
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
        List<String> urls = reviewRequest.urls();
        if(Objects.isNull(urls)){
            return;
        }
        moveImages(urls, reviewId);
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
        boardStatisticService.updateReviewCount(review.getBoardId(), review.getRate(), DELETE);
    }

    @Transactional
    public void deleteImage(Long imageId) {
        ReviewImg reviewImg = reviewImgRepository.findById(imageId)
                .orElseThrow(() -> new BbangleException(IMAGE_NOT_FOUND));
        reviewImgRepository.delete(reviewImg);
    }

    private boolean checkHasNext(int size) {
        return size >= PAGE_SIZE + 1;
    }

    @Transactional
    public SummarizedReviewResponse getSummarizedReview(Long boardId) {
        List<ReviewDto> reviews = reviewRepository.findByBoardId(boardId);

        if (reviews.isEmpty()) {
            return SummarizedReviewResponse.getEmpty();
        }

        BigDecimal averageRatingScore = reviewStatistics.getAverageRatingScore(reviews);
        int reviewCount = reviewStatistics.count(reviews);
        List<String> popularBadgeList = reviewStatistics.getPopularBadgeList(reviews);

        return SummarizedReviewResponse.of(averageRatingScore, reviewCount, popularBadgeList);
    }

    private String createNewStoragePath(String extractedFileName, Long reviewId){
        return ImageCategory.REVIEW.name().toLowerCase(ROOT)+"/"+reviewId+extractedFileName;
    }

    private void moveImages(List<String> urls, Long reviewId){
        List<Image> images = imageRepository.findAllByPathIn(urls);

        List<String> fromPaths = images.stream()
                .map(Image::getPath)
                .map(path -> path.replace(BUCKET_DOMAIN, ""))
                .toList();

        List<String> toPaths = fromPaths.stream()
                .map(path -> path.substring(path.lastIndexOf("/")))
                .map(extractedFileName -> createNewStoragePath(extractedFileName, reviewId))
                .toList();

        for(int i = 0; i < fromPaths.size(); i++){
            images.get(i).update(reviewId, CTN_DOMAIN + toPaths.get(i));
            imageService.move(fromPaths.get(i), toPaths.get(i));
        }
    }
}
