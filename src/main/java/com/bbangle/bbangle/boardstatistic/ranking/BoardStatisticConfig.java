package com.bbangle.bbangle.boardstatistic.ranking;

import com.bbangle.bbangle.boardstatistic.service.BoardPreferenceStatisticService;
import com.bbangle.bbangle.boardstatistic.service.BoardStatisticService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class BoardStatisticConfig {

    private final BoardStatisticService boardStatisticService;
    private final BoardPreferenceStatisticService preferenceStatisticService;

    @Transactional
    @PostConstruct
    public void init() {
        boardStatisticService.updatingNonRankedBoards();
        boardStatisticService.checkingStatisticStatusAndUpdate();
        preferenceStatisticService.updatingNonRankedBoards();
        preferenceStatisticService.checkingBasicScoreAndUpdate();
    }

}
