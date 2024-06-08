package com.bbangle.bbangle.store.service;

import com.bbangle.bbangle.board.dao.TagsDao;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.dto.TagCategoryDto;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.page.StoreDetailCustomPage;
import com.bbangle.bbangle.store.dto.BoardsInStoreDto;
import com.bbangle.bbangle.store.dto.BoardsInStoreResponse;

import com.bbangle.bbangle.page.StoreCustomPage;
import com.bbangle.bbangle.store.dto.StoreDetailStoreDto;
import com.bbangle.bbangle.store.dto.StoreResponseDto;
import com.bbangle.bbangle.store.repository.StoreRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreService {

    private static final Long PAGE_SIZE = 10L;

    private final StoreRepository storeRepository;
    private final BoardRepository boardRepository;
    private final ProductRepository productRepository;

    public StoreDetailStoreDto getStoreResponse(Long memberId, Long storeId) {
        return storeRepository.getStoreResponse(memberId, storeId);
    }

    public StoreDetailCustomPage<List<BoardsInStoreResponse>> getBoardsInStore(Long memberId,
        Long storeId,
        Long boardIdAsCursorId) {

        List<Long> boardIds = boardRepository.getBoardIds(boardIdAsCursorId, storeId);

        if (boardIds.isEmpty()) {
            return StoreDetailCustomPage.empty(boardIdAsCursorId);
        }

        List<BoardsInStoreDto> boards = boardRepository.findByBoardIds(boardIds, memberId);
        List<TagCategoryDto> tagCategorys = productRepository.getTagCategory(boardIds);

        Map<Long, List<TagsDao>> tags = getTags(tagCategorys);
        Map<Long, Set<Category>> categories = getCategories(tagCategorys);

        List<BoardsInStoreResponse> boardsInStoreResponse = combineBaseOnBoardId(boards, tags,
            categories);

        // 이 부분 통합 작업이 필요해 보임
        boolean hasNext = checkingHasNextStoreDetail(boardsInStoreResponse);

        if (hasNext) {
            boardsInStoreResponse.remove(boardsInStoreResponse.get(boardsInStoreResponse.size() - 1));
        }

        return StoreDetailCustomPage.from(boardsInStoreResponse, boardIds, hasNext);
    }

    public Map<Long, List<TagsDao>> getTags(List<TagCategoryDto> tagCategorys) {
        return tagCategorys.stream()
            .collect(Collectors.groupingBy(
                TagCategoryDto::getBoardId,
                Collectors.mapping(TagCategoryDto::getTags, Collectors.toList())
            ));
    }

    public Map<Long, Set<Category>> getCategories(List<TagCategoryDto> tagCategorys) {
        return tagCategorys.stream()
            .collect(Collectors.groupingBy(
                TagCategoryDto::getBoardId,
                Collectors.mapping(TagCategoryDto::getCategory, Collectors.toSet())
            ));
    }

    public List<BoardsInStoreResponse> combineBaseOnBoardId(
        List<BoardsInStoreDto> boardsInStoreDtos,
        Map<Long, List<TagsDao>> tags,
        Map<Long, Set<Category>> categories
    ) {
        return boardsInStoreDtos.stream()
            .map(boardListDto ->
                BoardsInStoreResponse.from(boardListDto,
                    tags.get(boardListDto.getBoardId()),
                    categories.get(boardListDto.getBoardId())))
            .collect(Collectors.toCollection(ArrayList::new));
    }


    public StoreCustomPage<List<StoreResponseDto>> getList(Long cursorId, Long memberId) {
        return storeRepository.getStoreList(cursorId, memberId);
    }

    private boolean checkingHasNextStoreDetail(
        List<BoardsInStoreResponse> storeDetailResponse) {
        return storeDetailResponse.size() >= PAGE_SIZE + 1;
    }


}