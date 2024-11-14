package com.bbangle.bbangle.board.service.solution;

import com.bbangle.bbangle.board.service.solution.domain.SolutionEnum;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SolutionContext {
    List<SolutionEnum> solutionEnums;

    Map<Long, EnumMap<SolutionEnum, List<Object>>> data;
    Map<Long, EnumMap<SolutionEnum, Object>> outputData;

    @Builder
    public SolutionContext(List<SolutionEnum> solutionEnums) {
        this.solutionEnums = solutionEnums;
        data = new HashMap<>();
        outputData = new HashMap<>();
    }

    void setData(Map<Long, EnumMap<SolutionEnum, List<Object>>> data) {
        this.data = data;
    }

    void setOutputData(Map<Long, EnumMap<SolutionEnum, Object>> outputData) {
        this.outputData = outputData;
    }
}
