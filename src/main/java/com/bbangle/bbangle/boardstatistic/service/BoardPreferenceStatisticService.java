package com.bbangle.bbangle.boardstatistic.service;

import com.bbangle.bbangle.board.customer.repository.dao.BoardWithTagDao;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.boardstatistic.domain.BoardPreferenceStatistic;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.boardstatistic.repository.BoardPreferenceStatisticRepository;
import com.bbangle.bbangle.boardstatistic.repository.BoardStatisticRepository;
import com.bbangle.bbangle.preference.domain.PreferenceType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardPreferenceStatisticService {

    private final BoardPreferenceStatisticRepository preferenceStatisticRepository;
    private final BoardStatisticRepository boardStatisticRepository;
    private final BoardRepository boardRepository;

    @Transactional
    public void updatingNonRankedBoards() {
        List<BoardWithTagDao> boardWithTagDaos = boardRepository.checkingNullWithPreferenceRanking();
        List<BoardPreferenceStatistic> boardPreferenceStatisticList = generateUnsavedPreferenceStatisticList(
            boardWithTagDaos);

        preferenceStatisticRepository.saveAll(boardPreferenceStatisticList);
    }

    private List<BoardPreferenceStatistic> generateUnsavedPreferenceStatisticList(
        List<BoardWithTagDao> boardWithTagDaos
    ) {
        Map<String, BoardPreferenceStatistic> preferenceMap = new HashMap<>();

        boardWithTagDaos.forEach(board -> Arrays.stream(PreferenceType.values())
            .forEach(preference -> {
                String key = board.boardId() + "_" + preference;
                int calculatedScore = preference.getCalculatedScore(board.tagsDao());

                if (preferenceMap.containsKey(key)) {
                    BoardPreferenceStatistic existingStatistic = preferenceMap.get(key);
                    existingStatistic.updateWeightWhileInitializing(
                        existingStatistic.getPreferenceWeight() + calculatedScore);
                } else {
                    BoardPreferenceStatistic newStatistic = BoardPreferenceStatistic.builder()
                        .preferenceType(preference)
                        .boardId(board.boardId())
                        .preferenceWeight(calculatedScore)
                        .build();
                    preferenceMap.put(key, newStatistic);
                }
            }));

        return preferenceMap.values().stream().toList();
    }

    @Transactional
    public void checkingBasicScoreAndUpdate() {
        List<BoardPreferenceStatistic> unmatchedBasicScore = preferenceStatisticRepository.findUnmatchedBasicScore();
        List<Long> unmatchedIdList = unmatchedBasicScore.stream()
            .map(BoardPreferenceStatistic::getBoardId)
            .toList();
        List<BoardStatistic> boardStatisticList = boardStatisticRepository.findAllByBoardIds(
            unmatchedIdList);
        for(BoardPreferenceStatistic preferenceStatistic : unmatchedBasicScore) {
            for(BoardStatistic basicStatistic : boardStatisticList){
                if(preferenceStatistic.getBoardId().equals(basicStatistic.getBoard().getId())){
                    preferenceStatistic.updateToBasicBoardScore(basicStatistic.getBasicScore());
                    preferenceStatistic.updatePreferenceScore();
                }
            }
        }
    }

}
