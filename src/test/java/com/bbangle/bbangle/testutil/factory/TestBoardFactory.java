package com.bbangle.bbangle.testutil.factory;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import jakarta.persistence.EntityManager;

public class TestBoardFactory extends TestModelFactory<Board, BoardRepository> {
    public TestBoardFactory(EntityManager entityManager, BoardRepository repository) {
        super(entityManager, repository);
    }

    @Override
    protected Board saveEntity(Board board) {
        return repository.save(board);
    }
}