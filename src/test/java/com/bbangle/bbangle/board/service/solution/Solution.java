package com.bbangle.bbangle.board.service.solution;

import com.bbangle.bbangle.board.service.domain.SolutionEnum;
import com.bbangle.bbangle.board.service.solution.utils.SolutionResolverFactory;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class Solution {

    public void resolver(SolutionContext context) {
        context.getData().forEach(boardMap ->
            boardMap.forEach((boardId, enumMap) -> getData(context, boardId, enumMap))
        );
    }

    private void getData(
        SolutionContext context,
        Long boardId,
        EnumMap<SolutionEnum, List<Object>> enumMap
    ) {
        EnumMap<SolutionEnum, Object> dataMap = new EnumMap<>(SolutionEnum.class);

        enumMap.forEach((solutionEnum, values) -> {
            Object transformedValue = transformValue(solutionEnum, values);
            if (transformedValue != null) {
                dataMap.put(solutionEnum, transformedValue);
            }
        });

        Map<Long, EnumMap<SolutionEnum, Object>> boardMap = new HashMap<>();
        boardMap.put(boardId, dataMap);

        context.getOutputData().add(boardMap);
    }

    private Object transformValue(SolutionEnum solutionEnum, List values) {
        return SolutionResolverFactory.getResolver(solutionEnum).resolve(values);
    }
}

