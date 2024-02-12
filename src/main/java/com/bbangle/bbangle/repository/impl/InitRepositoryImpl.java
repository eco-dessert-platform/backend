package com.bbangle.bbangle.repository.impl;

import com.bbangle.bbangle.model.QBoard;
import com.bbangle.bbangle.model.QStore;
import com.bbangle.bbangle.repository.InitRepository;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class InitRepositoryImpl implements InitRepository {
    private final JPAQueryFactory queryFactory;
    @Override
    public HashMap<Long, String> getAllBoardTitle() {
        QBoard board = QBoard.board;

        List<Tuple> fetch = queryFactory
                .select(board.id, board.title)
                .from(board)
                .fetch();

        HashMap<Long, String> boardMap = new HashMap<>();
        fetch.forEach((tuple) -> boardMap.put(tuple.get(board.id), tuple.get(board.title)));

        return boardMap;
    }

    @Override
    public HashMap<Long, String> getAllStoreTitle() {
        QStore store = QStore.store;

        List<Tuple> fetch = queryFactory
                .select(store.id, store.name)
                .from(store)
                .fetch();

        HashMap<Long, String> storeMap = new HashMap<>();
        fetch.forEach((tuple) -> storeMap.put(tuple.get(store.id), tuple.get(store.name)));

        return storeMap;
    }
}
