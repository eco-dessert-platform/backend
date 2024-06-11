package com.bbangle.bbangle.board.repository.util;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dao.TagsDao;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.page.BoardCustomPage;
import java.util.ArrayList;
import java.util.Collections;
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

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BoardPageGenerator {

    private static final Long NO_NEXT_CURSOR = -1L;
    private static final Long HAS_NEXT_PAGE_SIZE = BOARD_PAGE_SIZE + 1L;

    public static BoardCustomPage<List<BoardResponseDto>> getBoardPage(List<BoardResponseDao> boardDaos, Boolean isInFolder) {
        if (boardDaos.isEmpty()) {
            return BoardCustomPage.emptyPage();
        }

        List<BoardResponseDto> boardResponseDtos = convertToBoardResponse(boardDaos, isInFolder);

        Long nextCursor = NO_NEXT_CURSOR;
        boolean hasNext = false;
        if (boardResponseDtos.size() == HAS_NEXT_PAGE_SIZE) {
            hasNext = true;
            nextCursor = boardResponseDtos.get(boardResponseDtos.size() - 1).getBoardId();
        }
        boardResponseDtos = boardResponseDtos.stream().limit(BOARD_PAGE_SIZE).toList();

        return BoardCustomPage.from(boardResponseDtos, nextCursor, hasNext);
    }

    private static List<BoardResponseDto> convertToBoardResponse(
        List<BoardResponseDao> boardResponseDaoList,
        Boolean isInFolder
    ) {
        Map<Long, List<String>> tagMapByBoardId = getTagListFromBoardResponseDao(boardResponseDaoList);

        Map<Long, Boolean> isBundled = getIsBundled(boardResponseDaoList);

        boardResponseDaoList = removeDuplicatesByBoardId(boardResponseDaoList);

        return getBoardResponseDtos(boardResponseDaoList, isInFolder, isBundled, tagMapByBoardId);
    }

    private static List<BoardResponseDto> getBoardResponseDtos(
        List<BoardResponseDao> boardResponseDaoList,
        Boolean isInFolder,
        Map<Long, Boolean> isBundled,
        Map<Long, List<String>> tagMapByBoardId
    ) {
        if(isInFolder){
            return boardResponseDaoList.stream()
                .map(boardDao -> BoardResponseDto.inFolder(
                    boardDao,
                    isBundled.get(boardDao.boardId()),
                    tagMapByBoardId.get(boardDao.boardId()))
                )
                .toList();
        }

        return boardResponseDaoList.stream()
            .map(boardDao -> BoardResponseDto.from(
                boardDao,
                isBundled.get(boardDao.boardId()),
                tagMapByBoardId.get(boardDao.boardId()))
            )
            .toList();
    }

    private static Map<Long, List<String>> getTagListFromBoardResponseDao(List<BoardResponseDao> boardResponseDaoList) {
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

    private static List<BoardResponseDao> removeDuplicatesByBoardId(List<BoardResponseDao> boardResponseDaos) {
        Map<Long, BoardResponseDao> uniqueBoardMap = boardResponseDaos.stream()
            .collect(Collectors.toMap(
                BoardResponseDao::boardId,
                boardResponseDao -> boardResponseDao,
                (existing, replacement) -> existing,
                LinkedHashMap::new
            ));

        return uniqueBoardMap.values()
            .stream()
            .toList();
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

}
