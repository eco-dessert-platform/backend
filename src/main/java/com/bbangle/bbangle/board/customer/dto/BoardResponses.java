package com.bbangle.bbangle.board.customer.dto;

import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.repository.dao.BoardThumbnailDao;
import com.bbangle.bbangle.board.repository.dao.TagsDao;
import java.time.LocalDateTime;
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

public record BoardResponses(List<BoardResponse> boardResponses) {

    public static BoardResponses from(List<BoardThumbnailDao> boardThumbnailDaos) {
        Map<Long, List<String>> tagMapByBoardId = getTagListFromBoardResponseDao(
            boardThumbnailDaos);

        Map<Long, Boolean> isBundled = getIsBundled(boardThumbnailDaos);
        Map<Long, Boolean> isSoldOut = getIsSoldOut(boardThumbnailDaos);
        Map<Long, Boolean> isBbangcketing = getIsBbangcketing(boardThumbnailDaos);

        boardThumbnailDaos = removeDuplicatesByBoardId(boardThumbnailDaos);

        List<BoardResponse> boardResponses = getBoardResponseDtos(boardThumbnailDaos,
            isBundled, tagMapByBoardId,
            isSoldOut, isBbangcketing);

        clearLinkedHashMap(tagMapByBoardId, isBundled, isSoldOut, isBbangcketing);

        return new BoardResponses(boardResponses);
    }

    private static Map<Long, Boolean> getIsBbangcketing(
        List<BoardThumbnailDao> boardThumbnailDaoList) {
        return boardThumbnailDaoList.stream()
            .collect(Collectors.toMap(
                BoardThumbnailDao::boardId,
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

    private static Map<Long, Boolean> getIsSoldOut(List<BoardThumbnailDao> boardThumbnailDaoList) {
        return boardThumbnailDaoList.stream()
            .collect(Collectors.toMap(
                BoardThumbnailDao::boardId,
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

    private static List<BoardResponse> getBoardResponseDtos(
        List<BoardThumbnailDao> boardThumbnailDaoList,
        Map<Long, Boolean> isBundled,
        Map<Long, List<String>> tagMapByBoardId,
        Map<Long, Boolean> isSoldOut, Map<Long, Boolean> isBbangcketing
    ) {

        return boardThumbnailDaoList.stream()
            .map(boardDao -> BoardResponse.of(
                boardDao,
                isBundled.get(boardDao.boardId()),
                tagMapByBoardId.get(boardDao.boardId()),
                isBbangcketing.get(boardDao.boardId()),
                isSoldOut.get(boardDao.boardId()))
            )
            .toList();
    }

    private static Map<Long, List<String>> getTagListFromBoardResponseDao(
        List<BoardThumbnailDao> boardThumbnailDaoList
    ) {
        return boardThumbnailDaoList.stream()
            .collect(Collectors.toMap(
                BoardThumbnailDao::boardId,
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

    private static Map<Long, Boolean> getIsBundled(List<BoardThumbnailDao> boardThumbnailDaoList) {
        return boardThumbnailDaoList
            .stream()
            .collect(Collectors.toMap(
                BoardThumbnailDao::boardId,
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

    private static List<BoardThumbnailDao> removeDuplicatesByBoardId(
        List<BoardThumbnailDao> boardThumbnailDaos
    ) {
        Map<Long, BoardThumbnailDao> uniqueBoardMap = boardThumbnailDaos.stream()
            .collect(Collectors.toMap(
                BoardThumbnailDao::boardId,
                boardResponseDao -> boardResponseDao,
                (existing, replacement) -> existing,
                LinkedHashMap::new
            ));

        List<BoardThumbnailDao> resultList = uniqueBoardMap.values()
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

    public int size() {
        return boardResponses == null ? 0 : boardResponses.size();
    }
}
