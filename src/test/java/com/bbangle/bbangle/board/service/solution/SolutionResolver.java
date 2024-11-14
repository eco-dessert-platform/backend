package com.bbangle.bbangle.board.service.solution;

import com.bbangle.bbangle.board.service.solution.domain.SolutionEnum;
import com.bbangle.bbangle.board.service.solution.factory.SolutionResolverFactory;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class SolutionResolver {

    public void resolver(SolutionContext context) {
        Map<Long, EnumMap<SolutionEnum, Object>> boardMap = new HashMap<>();

        context.getData()
            .forEach((boardId, enumMap) -> getData(boardId, enumMap, boardMap));

        context.setOutputData(boardMap);
    }

    private void getData(
        Long boardId,
        EnumMap<SolutionEnum, List<Object>> enumMap,
        Map<Long, EnumMap<SolutionEnum, Object>> boardMap
    ) {
        EnumMap<SolutionEnum, Object> dataMap = new EnumMap<>(SolutionEnum.class);

        enumMap.forEach((solutionEnum, values) -> {
            Object transformedValue = transformValue(solutionEnum, values);
            if (Objects.nonNull(transformedValue)) {
                dataMap.put(solutionEnum, transformedValue);
            }
        });

        boardMap.put(boardId, dataMap);
    }

    private Object transformValue(SolutionEnum solutionEnum, List values) {
        return SolutionResolverFactory.getResolver(solutionEnum).resolve(values);
    }
}

