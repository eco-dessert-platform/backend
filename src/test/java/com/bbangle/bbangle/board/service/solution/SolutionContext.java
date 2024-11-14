package com.bbangle.bbangle.board.service.solution;

import com.bbangle.bbangle.board.service.domain.SolutionEnum;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SolutionContext {
    List<SolutionEnum> solutionEnums;

    List<Map<Long, EnumMap<SolutionEnum, List<Object>>>> data;
    List<Map<Long, EnumMap<SolutionEnum, Object>>> outputData;

    @Builder
    public SolutionContext(List<SolutionEnum> solutionEnums) {
        this.solutionEnums = solutionEnums;
        data = new ArrayList<>();
        outputData = new ArrayList<>();
    }
}
