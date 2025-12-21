package com.bbangle.bbangle.boardstatistic.customer.ranking;

import com.bbangle.bbangle.boardstatistic.customer.service.BoardPreferenceStatisticService;
import com.bbangle.bbangle.boardstatistic.customer.service.BoardStatisticService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Slf4j
@RequiredArgsConstructor
@Profile("!test")
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
