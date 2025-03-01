package com.bbangle.bbangle.board.repository.basic.query;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dao.QBoardResponseDao;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.boardstatistic.domain.QBoardPreferenceStatistic;
import com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.preference.domain.Preference;
import com.bbangle.bbangle.preference.domain.PreferenceType;
import com.bbangle.bbangle.preference.domain.QMemberPreference;
import com.bbangle.bbangle.preference.domain.QPreference;
import com.bbangle.bbangle.store.domain.QStore;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreferenceRecommendBoardQueryProviderResolver{

    private static final QBoard board = QBoard.board;
    private static final QProduct product = QProduct.product;
    private static final QStore store = QStore.store;
    private static final QBoardPreferenceStatistic preferenceBoardStatistic = QBoardPreferenceStatistic.boardPreferenceStatistic;
    private static final QBoardStatistic boardStatistic = QBoardStatistic.boardStatistic;

    private final JPAQueryFactory queryFactory;

    public List<BoardResponseDao> findBoards(
        BooleanBuilder filter,
        BooleanBuilder cursorInfo,
        OrderSpecifier<?>[] orderCondition,
        Long memberId,
        PreferenceType selectedPreference
    ) {
        List<Long> boardIds = queryFactory.select(board.id)
            .distinct()
            .from(product)
            .join(board)
            .on(product.board.id.eq(board.id))
            .join(preferenceBoardStatistic)
            .on(preferenceBoardStatistic.boardId.eq(board.id))
            .where(cursorInfo.and(filter).and(preferenceBoardStatistic.preferenceType.eq(selectedPreference)))
            .orderBy(orderCondition)
            .limit(BOARD_PAGE_SIZE + 1)
            .fetch();

        return queryFactory.select(
                new QBoardResponseDao(
                    board.id,
                    store.id,
                    store.name,
                    board.profile,
                    board.title,
                    board.price,
                    product.category,
                    product.glutenFreeTag,
                    product.highProteinTag,
                    product.sugarFreeTag,
                    product.veganTag,
                    product.ketogenicTag,
                    boardStatistic.boardReviewGrade,
                    boardStatistic.boardReviewCount,
                    product.orderEndDate,
                    product.soldout,
                    board.discountRate
                ))
            .from(product)
            .join(board)
            .on(product.board.id.eq(board.id))
            .join(store)
            .on(board.store.id.eq(store.id))
            .join(board.boardStatistic, boardStatistic)
            .join(preferenceBoardStatistic)
            .on(board.id.eq(preferenceBoardStatistic.boardId))
            .where(board.id.in(boardIds))
            .where(preferenceBoardStatistic.preferenceType.eq(selectedPreference))
            .orderBy(orderCondition)
            .fetch();
    }

}
