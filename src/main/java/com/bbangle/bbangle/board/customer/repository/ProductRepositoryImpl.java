package com.bbangle.bbangle.board.customer.repository;

import com.bbangle.bbangle.board.customer.dto.QAiLearningProductDto;
import com.bbangle.bbangle.board.customer.dto.QTitleDto;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.board.customer.dto.AiLearningProductDto;
import com.bbangle.bbangle.board.customer.dto.TitleDto;
import com.bbangle.bbangle.board.customer.dto.orders.ProductDtoAtBoardDetail;
import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.QPush;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.HashMap;
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
    private static final QPush push = QPush.push;

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
    public List<TitleDto> findAllTitle() {
        return queryFactory.select(
                new QTitleDto(
                    product.board.id,
                    product.title))
            .from(product)
            .orderBy(board.id.desc())
            .fetch();
    }

    @Override
    public List<Product> findByBoardId(Long boardId) {
        return queryFactory
            .selectFrom(product)
            .where(product.board.id.eq(boardId))
            .fetch();
    }

    @Override
    public Map<Long, Push> findPushByProductIds(List<Long> productIds, Long memberId) {
        List<Push> pushList = queryFactory.selectFrom(push)
            .where(
                Expressions.allOf(
                    push.memberId.eq(memberId),
                    push.productId.in(productIds),
                    push.isActive.isTrue()
                )
            )
            .fetch();

        if (pushList.isEmpty()) {
            return Collections.emptyMap();
        } else {
            Map<Long, Push> maps = new HashMap<>();

            pushList.forEach(push1 -> maps.put(push1.getProductId(), push1));

            return maps;
        }
    }

    @Override
    public List<ProductDtoAtBoardDetail> findProductDtoById(Long memberId, Long boardId) {
        return queryFactory
            .select(
                Projections.constructor(
                    ProductDtoAtBoardDetail.class,
                    product.id,
                    product.title,
                    product.price,
                    product.category,
                    product.glutenFreeTag,
                    product.highProteinTag,
                    product.sugarFreeTag,
                    product.veganTag,
                    product.ketogenicTag,
                    product.nutrition.sugars,
                    product.nutrition.protein,
                    product.nutrition.carbohydrates,
                    product.nutrition.fat,
                    product.nutrition.weight,
                    product.nutrition.calories,
                    product.monday,
                    product.tuesday,
                    product.wednesday,
                    product.thursday,
                    product.friday,
                    product.saturday,
                    product.sunday,
                    product.orderStartDate,
                    product.orderEndDate,
                    product.soldout,
                    push.pushType,
                    push.days,
                    push.isActive
                ))
            .from(product)
            .leftJoin(push).on(
                product.id.eq(push.productId)
                    .and(memberId != null ? push.memberId.eq(memberId)
                        : Expressions.booleanTemplate("false"))
                    .and(push.isActive.eq(true)))
            .where(product.board.id.eq(boardId))
            .orderBy(product.id.desc())
            .fetch();
    }

    @Override
    public List<AiLearningProductDto> findAiLearningData() {
        return queryFactory.select(
                new QAiLearningProductDto(
                    product.board.id,
                    product.id,
                    product.title
                )
            ).from(product)
            .orderBy(product.board.id.asc())
            .fetch();
    }
}
