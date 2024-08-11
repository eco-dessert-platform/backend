package com.bbangle.bbangle.boardstatistic.service;

import com.bbangle.bbangle.board.dao.BoardWithTagDao;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.boardstatistic.domain.BoardPreferenceStatistic;
import com.bbangle.bbangle.boardstatistic.repository.BoardPreferenceStatisticRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardPreferenceStatisticService {

    private final BoardPreferenceStatisticRepository preferenceStatisticRepository;
    private final BoardRepository boardRepository;

    public void updatingNonRankedBoards() {
        List<BoardWithTagDao> boardWithTagDaos = boardRepository.checkingNullWithPreferenceRanking();
        List<BoardPreferenceStatistic> boardPreferenceStatistics = new ArrayList<>();


    }

}
