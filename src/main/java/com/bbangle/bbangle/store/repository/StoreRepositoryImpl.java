package com.bbangle.bbangle.store.repository;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.store.domain.QStore.store;

import com.bbangle.bbangle.board.dto.AiLearningStoreDto;
import com.bbangle.bbangle.board.dto.QAiLearningStoreDto;
import com.bbangle.bbangle.store.dto.QStoreDto;
import com.bbangle.bbangle.store.dto.StoreDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreQueryDSLRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public StoreDto findByBoardId(Long boardId) {
        return queryFactory.select(
                new QStoreDto(
                    store.id,
                    store.name,
                    store.profile
                )
            ).from(board)
            .join(store).on(store.eq(board.store))
            .where(board.id.eq(boardId))
            .fetchFirst();
    }

    @Override
    public List<AiLearningStoreDto> findAiLearningData() {
        return queryFactory.select(
                new QAiLearningStoreDto(
                    store.id,
                    board.id,
                    store.introduce
                )
            ).from(board)
            .join(board.store, store)
            .orderBy(store.id.asc())
            .fetch();
    }
}
