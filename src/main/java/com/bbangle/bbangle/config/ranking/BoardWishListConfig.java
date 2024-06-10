package com.bbangle.bbangle.config.ranking;

import com.bbangle.bbangle.boardstatistic.service.BoardStatisticService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class BoardWishListConfig {

    private final BoardStatisticService boardStatisticService;

    @PostConstruct
    public void init() {
        boardStatisticService.updatingNonRankedBoards();
    }

}
