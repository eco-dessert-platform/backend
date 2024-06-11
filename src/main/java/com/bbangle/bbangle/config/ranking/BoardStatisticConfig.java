package com.bbangle.bbangle.config.ranking;

import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.boardstatistic.repository.BoardStatisticRepository;
import com.bbangle.bbangle.boardstatistic.service.BoardStatisticService;
import com.bbangle.bbangle.review.domain.QReview;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class BoardStatisticConfig {

    private final BoardStatisticService boardStatisticService;

    @Transactional
    @Async
    @PostConstruct
    public void init() {
        boardStatisticService.updatingNonRankedBoards();
        boardStatisticService.checkingStatisticStatusAndUpdate();
    }

}
