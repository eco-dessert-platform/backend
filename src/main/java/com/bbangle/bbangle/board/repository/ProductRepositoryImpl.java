package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.board.dto.TagCategoryDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
                product.board.id,
                product.category
            )
            .from(product)
            .where(product.board.id.in(boardIds))
            .fetch().stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(product.board.id), // key
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

    @Override
    public List<TagCategoryDto> getTagCategory(List<Long> boardIds) {

        return queryFactory.select(
                Projections.constructor(
                    TagCategoryDto.class,
                    product.board.id,
                    product.glutenFreeTag,
                    product.sugarFreeTag,
                    product.highProteinTag,
                    product.veganTag,
                    product.ketogenicTag,
                    product.category
                )
            )
            .from(product)
            .where(product.board.id.in(boardIds))
            .fetch();
    }


    @Override
    public Set<Long> findProductJustStocked(List<Long> subscribedProductIdList) {
        return new HashSet<>(queryFactory.select(product.id)
                .from(product)
                .where(product.soldout.isFalse()
                        .and(product.id.in(subscribedProductIdList))
                )
                .fetch());
    }

}
