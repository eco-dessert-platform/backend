package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.boardstatistic.domain.BoardPreferenceStatistic;
import com.bbangle.bbangle.preference.domain.PreferenceType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BoardPreferenceStatisticFixture {

    public static List<BoardPreferenceStatistic> createBasicPreferenceStatistic(Long boardId){
        List<BoardPreferenceStatistic> boardPreferenceStatistics = new ArrayList<>();
        Arrays.stream(PreferenceType.values())
            .forEach(preference -> {
                BoardPreferenceStatistic preferenceStatistic = BoardPreferenceStatistic.builder()
                    .preferenceType(preference)
                    .boardId(boardId)
                    .build();
                boardPreferenceStatistics.add(preferenceStatistic);
            });
        return boardPreferenceStatistics;
    }

}
