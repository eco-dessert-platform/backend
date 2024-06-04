package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductQueryDSLRepository {

    private static final QBoard board = QBoard.board;
    private static final QProduct product = QProduct.product;

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<Long, Set<Category>> getCategoryInfoByBoardId(List<Long> boardIds) {
        return queryFactory
            .select(
                board.id,
                product.category
            )
            .from(product)
            .leftJoin(product.board)
            .on(product.board.id.eq(board.id))
            .where(product.board.id.in(boardIds))
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(board.id), // key
                tuple -> { // value
                    Set<Category> categories = new HashSet<>();
                    categories.add(tuple.get(product.category));
                    return categories;
                },
                (existCategories, newCategory) -> { // 키 중복처리
                    existCategories.addAll(newCategory);
                    return existCategories;
                }
            ));
    }
}
