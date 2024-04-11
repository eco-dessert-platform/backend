package com.bbangle.bbangle.board.service;


import static com.bbangle.bbangle.exception.BbangleErrorCode.NOTFOUND_MEMBER;

import com.bbangle.bbangle.board.dto.BoardDetailResponseDto;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.common.image.repository.ObjectStorageRepository;
import com.bbangle.bbangle.common.sort.SortType;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.util.RedisKeyUtil;
import com.bbangle.bbangle.util.SecurityUtils;
import com.bbangle.bbangle.wishListFolder.domain.WishlistFolder;
import com.bbangle.bbangle.wishListFolder.repository.WishListFolderRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final WishListFolderRepository folderRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectStorageRepository objectStorageRepository;
    private final StoreRepository storeRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET_NAME;
    private final String DETAIL_HTML_FILE_NAME = "detail.html";

    public BoardServiceImpl(
        @Autowired
        BoardRepository boardRepository,
        @Autowired
        MemberRepository memberRepository,
        @Autowired
        WishListFolderRepository folderRepository,
        @Autowired
        @Qualifier("defaultRedisTemplate")
        RedisTemplate<String, Object> redisTemplate,
        ObjectStorageRepository objectStorageRepository,
        @Autowired
        StoreRepository storeRepository
    ) {
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
        this.folderRepository = folderRepository;
        this.redisTemplate = redisTemplate;
        this.objectStorageRepository = objectStorageRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public BoardCustomPage<List<BoardResponseDto>> getBoardList(
        FilterRequest filterRequest,
        SortType sort,
        Long cursorId
    ) {

        List<Long> matchedIdx = getListAdaptingSort(sort);
        BoardCustomPage<List<BoardResponseDto>> boardResponseDto = boardRepository.getBoardResponseDto(
            filterRequest,
            matchedIdx,
            cursorId
        );

        if (SecurityUtils.isLogin()) {
            List<BoardResponseDto> likeUpdatedDto = boardRepository.updateLikeStatus(matchedIdx,
                boardResponseDto.getContent());
            boardResponseDto.updateBoardLikeStatus(likeUpdatedDto);
        }

        return boardResponseDto;
    }


    @Override
    @Transactional(readOnly = true)
    public BoardDetailResponseDto getBoardDetailResponse(Long memberId, Long boardId) {
      return boardRepository.getBoardDetailResponse(memberId, boardId);
    }

    public Slice<BoardResponseDto> getPostInFolder(
        Long memberId,
        String sort,
        Long folderId,
        Pageable pageable
    ) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BbangleException(NOTFOUND_MEMBER));

        WishlistFolder folder = folderRepository.findByMemberAndId(member, folderId)
            .orElseThrow(() -> new BbangleException("존재하지 않는 폴더입니다."));

        return boardRepository.getAllByFolder(sort, pageable, folderId, folder);
    }

    private List<Long> getListAdaptingSort(
        SortType sort
    ) {
        if (sort != null && sort.equals(SortType.POPULAR)) {
            return getPopularIdList();
        }

        return getRecommentIdList();
    }

    private List<Long> getRecommentIdList() {
        return Objects.requireNonNull(redisTemplate.opsForZSet()
                .reverseRange(RedisKeyUtil.RECOMMEND_KEY, 0, -1))
            .stream()
            .map(value -> Long.valueOf(String.valueOf(value)))
            .toList();
    }

    private List<Long> getPopularIdList() {
        return Objects.requireNonNull(redisTemplate.opsForZSet()
                .reverseRange(RedisKeyUtil.POPULAR_KEY, 0, -1))
            .stream()
            .map(value -> Long.valueOf(String.valueOf(value)))
            .toList();
    }
}
