package com.bbangle.bbangle.board.repository.util;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dao.TagsDao;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.page.BoardCustomPage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BoardPageGenerator {

    private static final Long NO_NEXT_CURSOR = -1L;
    private static final Long HAS_NEXT_PAGE_SIZE = BOARD_PAGE_SIZE + 1L;

    /**
     *
     * 책임:
     *          1. list가 비었다면 빈 페이지를 반환함     -> 공통 CustomPage에서 커버가능
     *          2. Dao -> DTO 변환
     *          3. 커서 조절 및 DTO리스트 하나 삭제       -> 공통 CustomPage에서 커버가능
     *
     * 거슬리는 점:
     *              1. 플래그를 전달받음
     *              2. 함수가 20줄을 넘는다(아주 사소)
     *              3. 나머지는 쿼리로 한 번에 받지 않았기에 함수가 복잡하다(중요)
     */
    public static BoardCustomPage<List<BoardResponseDto>> getBoardPage(
        List<BoardResponseDao> boardDaos, Boolean isInFolder
    ) {
        if (boardDaos.isEmpty()) {
            return BoardCustomPage.emptyPage();
        }

        List<BoardResponseDto> boardResponseDtos = convertToBoardResponse(boardDaos, isInFolder);

        Long nextCursor = NO_NEXT_CURSOR;
        boolean hasNext = false;
        if (boardResponseDtos.size() == HAS_NEXT_PAGE_SIZE) {
            hasNext = true;
            nextCursor = boardResponseDtos.get(boardResponseDtos.size() - 1)
                .getBoardId();
        }
        boardResponseDtos = boardResponseDtos.stream()
            .limit(BOARD_PAGE_SIZE)
            .toList();

        return BoardCustomPage.from(boardResponseDtos, nextCursor, hasNext);
    }

    private static List<BoardResponseDto> convertToBoardResponse(
        List<BoardResponseDao> boardResponseDaoList,
        Boolean isInFolder
    ) {
        Map<Long, List<String>> tagMapByBoardId = getTagListFromBoardResponseDao(
            boardResponseDaoList);

        Map<Long, Boolean> isBundled = getIsBundled(boardResponseDaoList);
        Map<Long, Boolean> isSoldOut = getIsSoldOut(boardResponseDaoList);
        Map<Long, Boolean> isBbangcketing = getIsBbangcketing(boardResponseDaoList);

        boardResponseDaoList = removeDuplicatesByBoardId(boardResponseDaoList);

        List<BoardResponseDto> boardResponseDtos = getBoardResponseDtos(boardResponseDaoList,
            isInFolder, isBundled, tagMapByBoardId,
            isSoldOut, isBbangcketing);

        clearLinkedHashMap(tagMapByBoardId, isBundled, isSoldOut, isBbangcketing);

        return boardResponseDtos;
    }

    private static Map<Long, Boolean> getIsBbangcketing(List<BoardResponseDao> boardResponseDaoList) {
        return boardResponseDaoList.stream()
            .collect(Collectors.toMap(
                BoardResponseDao::boardId,
                board -> new ArrayList<>(Collections.singletonList(board.orderStartDate())),
                (existingList, newList) -> {
                    existingList.addAll(newList);
                    return existingList;
                }))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Entry::getKey,
                entry -> {
                    for (LocalDateTime endDate : entry.getValue()) {
                        if (endDate != null && endDate.isAfter(LocalDateTime.now())) {
                            return true;
                        }
                    }
                    return false;
                }
            ));
    }

    private static Map<Long, Boolean> getIsSoldOut(List<BoardResponseDao> boardResponseDaoList) {
        return boardResponseDaoList.stream()
            .collect(Collectors.toMap(
                BoardResponseDao::boardId,
                board -> new ArrayList<>(Collections.singletonList(board.isSoldOut())),
                (existingList, newList) -> {
                    existingList.addAll(newList);
                    return existingList;
                }))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Entry::getKey,
                entry -> {
                    for (Boolean isSoldOut : entry.getValue()) {
                        if (isSoldOut != null && !isSoldOut) {
                            return false;
                        }
                    }

                    return true;
                }
            ));
    }

    private static List<BoardResponseDto> getBoardResponseDtos(
        List<BoardResponseDao> boardResponseDaoList,
        Boolean isInFolder,
        Map<Long, Boolean> isBundled,
        Map<Long, List<String>> tagMapByBoardId,
        Map<Long, Boolean> isSoldOut, Map<Long, Boolean> isBbangcketing
    ) {
        if (Boolean.TRUE.equals(isInFolder)) {
            return boardResponseDaoList.stream()
                .map(boardDao -> BoardResponseDto.inFolder(
                    boardDao,
                    isBundled.get(boardDao.boardId()),
                    tagMapByBoardId.get(boardDao.boardId()),
                    isBbangcketing.get(boardDao.boardId()),
                    isSoldOut.get(boardDao.boardId()))
                )
                .toList();
        }

        return boardResponseDaoList.stream()
            .map(boardDao -> BoardResponseDto.from(
                boardDao,
                isBundled.get(boardDao.boardId()),
                tagMapByBoardId.get(boardDao.boardId()),
                isBbangcketing.get(boardDao.boardId()),
                isSoldOut.get(boardDao.boardId()))
            )
            .toList();
    }

    private static Map<Long, List<String>> getTagListFromBoardResponseDao(
        List<BoardResponseDao> boardResponseDaoList
    ) {
        return boardResponseDaoList.stream()
            .collect(Collectors.toMap(
                BoardResponseDao::boardId,
                board -> new ArrayList<>(Collections.singletonList(board.tagsDao())),
                (existingList, newList) -> {
                    existingList.addAll(newList);
                    return existingList;
                }))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Entry::getKey,
                entry -> extractTags(entry.getValue())
                    .stream()
                    .toList()
            ));
    }

    private static Map<Long, Boolean> getIsBundled(List<BoardResponseDao> boardResponseDaoList) {
        return boardResponseDaoList
            .stream()
            .collect(Collectors.toMap(
                BoardResponseDao::boardId,
                board -> new HashSet<>(Collections.singleton(board.category())),
                (existingSet, newSet) -> {
                    existingSet.addAll(newSet);
                    return existingSet;
                }
            ))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Entry::getKey,
                entry -> entry.getValue()
                    .size() > 1)
            );
    }

    private static List<BoardResponseDao> removeDuplicatesByBoardId(
        List<BoardResponseDao> boardResponseDaos
    ) {
        Map<Long, BoardResponseDao> uniqueBoardMap = boardResponseDaos.stream()
            .collect(Collectors.toMap(
                BoardResponseDao::boardId,
                boardResponseDao -> boardResponseDao,
                (existing, replacement) -> existing,
                LinkedHashMap::new
            ));

        List<BoardResponseDao> resultList = uniqueBoardMap.values()
            .stream()
            .toList();

        uniqueBoardMap.clear();
        return resultList;
    }

    private static List<String> extractTags(List<TagsDao> tagsDaoList) {
        if (Objects.isNull(tagsDaoList) || tagsDaoList.isEmpty()) {
            return Collections.emptyList();
        }

        HashSet<String> tags = new HashSet<>();
        for (TagsDao dto : tagsDaoList) {
            addTagIfTrue(tags, dto.glutenFreeTag(), TagEnum.GLUTEN_FREE.label());
            addTagIfTrue(tags, dto.highProteinTag(), TagEnum.HIGH_PROTEIN.label());
            addTagIfTrue(tags, dto.sugarFreeTag(), TagEnum.SUGAR_FREE.label());
            addTagIfTrue(tags, dto.veganTag(), TagEnum.VEGAN.label());
            addTagIfTrue(tags, dto.ketogenicTag(), TagEnum.KETOGENIC.label());
        }
        return new ArrayList<>(tags);
    }

    private static void addTagIfTrue(Set<String> tags, boolean condition, String tag) {
        if (condition) {
            tags.add(tag);
        }
    }

    private static void clearLinkedHashMap(
        Map<Long, List<String>> tagMapByBoardId,
        Map<Long, Boolean> isBundled,
        Map<Long, Boolean> isSoldOut,
        Map<Long, Boolean> isBbangcketing
    ) {
        tagMapByBoardId.clear();
        isBundled.clear();
        isSoldOut.clear();
        isBbangcketing.clear();
    }

}
