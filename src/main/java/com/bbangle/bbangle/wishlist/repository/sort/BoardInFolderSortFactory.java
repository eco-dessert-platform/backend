package com.bbangle.bbangle.wishlist.repository.sort;

import com.bbangle.bbangle.wishlist.repository.sort.strategy.BoardInFolderSortRepository;
import com.bbangle.bbangle.board.domain.constant.FolderBoardSortType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BoardInFolderSortFactory {

    private final Map<FolderBoardSortType, BoardInFolderSortRepository> strategyMap;

    /**
     * 모든 BoardSortStrategy 구현체를 받아서 SortType에 맞게 맵에 등록합니다.
     */
    @Autowired
    public BoardInFolderSortFactory(List<BoardInFolderSortRepository> strategies) {
        this.strategyMap = new EnumMap<>(FolderBoardSortType.class);
        strategies.forEach(strategy ->
                strategyMap.put(strategy.getSortType(), strategy)
        );
    }

    /**
     * 주어진 SortType에 맞는 전략을 반환합니다. 없는 경우 기본값(RECENT)을 반환합니다.
     */
    public BoardInFolderSortRepository getStrategy(FolderBoardSortType sortType) {
        return strategyMap.getOrDefault(sortType, strategyMap.get(FolderBoardSortType.WISHLIST_RECENT));
    }
}
