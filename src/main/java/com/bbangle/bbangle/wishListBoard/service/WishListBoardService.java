package com.bbangle.bbangle.wishListBoard.service;

import com.bbangle.bbangle.config.ranking.BoardLikeInfo;
import com.bbangle.bbangle.config.ranking.ScoreType;
import com.bbangle.bbangle.wishListBoard.dto.WishProductRequestDto;
import com.bbangle.bbangle.exception.MemberNotFoundException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.wishListFolder.domain.WishlistFolder;
import com.bbangle.bbangle.wishListBoard.domain.WishlistProduct;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.wishListFolder.repository.WishListFolderRepository;
import com.bbangle.bbangle.wishListBoard.repository.WishListProductRepository;
import com.bbangle.bbangle.util.RedisKeyUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishListBoardService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH");


    private final MemberRepository memberRepository;
    private final WishListFolderRepository wishListFolderRepository;
    private final WishListProductRepository wishListProductRepository;
    private final BoardRepository boardRepository;
    @Qualifier("defaultRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    @Qualifier("boardLikeInfoRedisTemplate")
    private final RedisTemplate<String, Object> boardLikeInfoRedisTemplate;

    @Transactional
    public void wish(Long memberId, Long boardId, WishProductRequestDto wishRequest) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(MemberNotFoundException::new);

        WishlistFolder wishlistFolder = wishListFolderRepository.findByMemberAndId(member,
                wishRequest.folderId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));

        wishListProductRepository.findByBoardAndFolderId(boardId, wishlistFolder)
            .ifPresentOrElse(
                product -> {
                    if(!product.isDeleted()){
                        throw new IllegalArgumentException("이미 위시리스트 폴더에 있는 게시물입니다.");
                    }
                    boolean status = product.updateWishStatus();
                    product.getBoard()
                        .updateWishCnt(status);

                    if (status) {
                        redisTemplate.opsForZSet()
                            .incrementScore(RedisKeyUtil.POPULAR_KEY, String.valueOf(boardId), 1);
                        redisTemplate.opsForZSet()
                            .incrementScore(RedisKeyUtil.RECOMMEND_KEY, String.valueOf(boardId), 1);
                        boardLikeInfoRedisTemplate.opsForList()
                            .rightPush(LocalDateTime.now()
                                    .format(formatter),
                                new BoardLikeInfo(boardId, 1, LocalDateTime.now(), ScoreType.WISH));
                    }
                },
                makeNewWish(boardId, wishlistFolder, member)
            );
    }

    private Runnable makeNewWish(Long boardId, WishlistFolder wishlistFolder, Member member) {
        return () -> {
            redisTemplate.opsForZSet()
                .incrementScore(RedisKeyUtil.POPULAR_KEY, String.valueOf(boardId), 1);
            redisTemplate.opsForZSet()
                .incrementScore(RedisKeyUtil.RECOMMEND_KEY, String.valueOf(boardId), 1);
            boardLikeInfoRedisTemplate.opsForList()
                .rightPush(LocalDateTime.now()
                        .format(formatter),
                    new BoardLikeInfo(boardId, 1, LocalDateTime.now(), ScoreType.WISH));
            Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
            WishlistProduct wishlistProduct = WishlistProduct.builder()
                .wishlistFolder(wishlistFolder)
                .board(board)
                .memberId(member.getId())
                .isDeleted(false)
                .build();
            WishlistProduct save = wishListProductRepository.save(wishlistProduct);
            save.getBoard()
                .updateWishCnt(true);
        };
    }

    @Transactional
    public void deletedByDeletedMember(Long memberId) {
        Optional<List<WishlistProduct>> wishlistProducts = wishListProductRepository.findByMemberId(
            memberId);
        if (wishlistProducts.isPresent()) {
            for (WishlistProduct wishlistProduct : wishlistProducts.get()) {
                wishlistProduct.delete();
            }
        }
    }

    @Transactional
    public void cancel(Long memberId, Long boardId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(MemberNotFoundException::new);

        WishlistProduct product = wishListProductRepository.findByBoardId(boardId, memberId)
            .orElseThrow(() -> new IllegalArgumentException("사용자의 위시리스트 항목에 존재하지 않는 게시글입니다."));

        if (product.isDeleted()) {
            throw new IllegalArgumentException("이미 위시리스트 항목에서 삭제된 게시글입니다.");
        }

        product.updateWishStatus();

        redisTemplate.opsForZSet()
            .incrementScore(RedisKeyUtil.POPULAR_KEY, String.valueOf(boardId), -1);
        redisTemplate.opsForZSet()
            .incrementScore(RedisKeyUtil.RECOMMEND_KEY, String.valueOf(boardId),
                -1);
        boardLikeInfoRedisTemplate.opsForList()
            .rightPush(LocalDateTime.now()
                    .format(formatter),
                new BoardLikeInfo(boardId, 1, LocalDateTime.now(), ScoreType.WISH));
    }

}
