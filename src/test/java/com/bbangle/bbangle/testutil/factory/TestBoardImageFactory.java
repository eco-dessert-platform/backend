package com.bbangle.bbangle.testutil.factory;

import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.board.repository.BoardImgRepository;
import jakarta.persistence.EntityManager;

public class TestBoardImageFactory extends TestModelFactory<ProductImg, BoardImgRepository> {

    public TestBoardImageFactory(EntityManager entityManager, BoardImgRepository repository) {
        super(entityManager, repository);
    }

    @Override
    protected ProductImg saveEntity(ProductImg productImg) {
        return repository.save(productImg);
    }
}
