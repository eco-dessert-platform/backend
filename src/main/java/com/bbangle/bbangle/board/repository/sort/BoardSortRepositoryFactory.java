package com.bbangle.bbangle.board.repository.sort;

import com.bbangle.bbangle.board.repository.sort.strategy.BoardSortRepository;
import com.bbangle.bbangle.board.sort.SortType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BoardSortRepositoryFactory {

    private final Map<SortType, BoardSortRepository> strategyMap;

    @Autowired
    public BoardSortRepositoryFactory(List<BoardSortRepository> strategies) {
        this.strategyMap = new EnumMap<>(SortType.class);
        strategies.forEach(strategy ->
                strategyMap.put(strategy.getSortType(), strategy)
        );
    }

    /**
     * 주어진 SortType에 맞는 전략을 반환합니다. 없는 경우 기본값(RECENT)을 반환합니다.
     */
    public BoardSortRepository getStrategy(SortType sortType) {
        return strategyMap.getOrDefault(sortType, strategyMap.get(SortType.RECENT));
    }
}
