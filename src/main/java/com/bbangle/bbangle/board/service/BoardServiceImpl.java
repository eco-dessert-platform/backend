package com.bbangle.bbangle.board.service;


import com.bbangle.bbangle.board.dto.BoardDetailResponseDto;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.exception.MemberNotFoundException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.common.sort.SortType;
import com.bbangle.bbangle.page.CursorInfo;
import com.bbangle.bbangle.page.CustomPage;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.wishListFolder.domain.WishlistFolder;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.common.image.repository.ObjectStorageRepository;
import com.bbangle.bbangle.wishListFolder.repository.WishListFolderRepository;
import com.bbangle.bbangle.util.RedisKeyUtil;
import com.bbangle.bbangle.util.SecurityUtils;
import java.util.Comparator;
import java.util.List;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
        RedisTemplate<String, Object> redisTemplate, ObjectStorageRepository objectStorageRepository,
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
    public CustomPage<List<BoardResponseDto>> getBoardList(
        String sort, Boolean glutenFreeTag, Boolean highProteinTag,
        Boolean sugarFreeTag, Boolean veganTag, Boolean ketogenicTag,
        String category, Integer minPrice, Integer maxPrice, Boolean orderAvailableToday,
        Long cursorId, CursorInfo cursorInfo
    ) {

        List<Long> matchedIdx = getListAdaptingSort(sort, cursorInfo.rank(), cursorInfo.endSize());
        List<BoardResponseDto> boardResponseDto = boardRepository.getBoardResponseDto(
            sort,
            glutenFreeTag,
            highProteinTag,
            sugarFreeTag,
            veganTag,
            ketogenicTag,
            category,
            minPrice,
            maxPrice,
            orderAvailableToday,
            matchedIdx
        );

        if (SecurityUtils.isLogin()) {
            boardResponseDto = boardRepository.updateLikeStatus(matchedIdx, boardResponseDto);
        }

        return getCustomPage(cursorInfo.rank(), cursorId, boardResponseDto, cursorInfo.hasNext());
    }

    private CustomPage<List<BoardResponseDto>> getCustomPage(
        Long rank,
        Long cursorId,
        List<BoardResponseDto> sortedResponse,
        boolean hasNext
    ) {
        if(rank.equals(0L)){
            long boardCnt = boardRepository.count();
            long storeCnt = storeRepository.count();
            return CustomPage.from(sortedResponse, cursorId, hasNext, boardCnt, storeCnt);
        }

        return CustomPage.from(sortedResponse, cursorId, hasNext);
    }

    private List<Long> getListAdaptingSort(
        String sort,
        Long cursorId,
        Long endSize
    ) {
        if (sort != null && sort.equals(SortType.POPULAR.getValue())) {
            return getPopularIdList(cursorId, endSize);

        }
        return getRecommentIdList(cursorId, endSize);
    }

    private List<Long> getRecommentIdList(
        Long startIdx,
        Long endSize
    ) {
        Set<Object> objects = redisTemplate.opsForZSet()
            .reverseRange(RedisKeyUtil.RECOMMEND_KEY, startIdx, endSize);

        return objects.stream()
            .map(idx -> Long.valueOf(String.valueOf(idx)))
            .toList();
    }

    private List<Long> getPopularIdList(
        Long startIdx,
        Long endSize
    ) {
        return redisTemplate.opsForZSet()
            .reverseRange(RedisKeyUtil.POPULAR_KEY, startIdx, -endSize)
            .stream()
            .map(idx -> Long.valueOf(String.valueOf(idx)))
            .toList();

    }

    @Override
    @Transactional(readOnly = true)
    public BoardDetailResponseDto getBoardDetailResponse(Long memberId, Long boardId) {

        return boardRepository.getBoardDetailResponse(memberId, boardId);
    }

    @Override
    @Transactional
    public Boolean saveBoardDetailHtml(Long boardId, MultipartFile htmlFile) {
//        Long storeId = boardRepository.findById(boardId)
//            .get()
//            .getStore()
//            .getId();
//        String filePath = String.format("%s/%s/%s", storeId, boardId, DETAIL_HTML_FILE_NAME);
//        // Board DetailUrl FilePath로 수정
//        if (boardRepository.updateDetailWhereStoreIdEqualsBoardId(
//            boardId,
//            filePath
//        ) != 1) {
//            return false;
//        }

        // ObjectStorage에 파일 생성
//        return objectStorageRepository.createFile(BUCKET_NAME, filePath, htmlFile);
        return null;
    }

    public Slice<BoardResponseDto> getPostInFolder(
        Long memberId,
        String sort,
        Long folderId,
        Pageable pageable
    ) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(MemberNotFoundException::new);

        WishlistFolder folder = folderRepository.findByMemberAndId(member, folderId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));

        return boardRepository.getAllByFolder(sort, pageable, folderId, folder);
    }

}
