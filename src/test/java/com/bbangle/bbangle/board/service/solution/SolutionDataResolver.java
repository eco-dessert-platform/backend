package com.bbangle.bbangle.board.service.solution;

import com.bbangle.bbangle.board.service.solution.domain.SolutionEnum;
import com.bbangle.bbangle.board.service.solution.utils.SolutionReflectionUtil;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SolutionDataResolver {

    public <T> void resolver(
        SolutionContext context,
        Map<Long, List<T>> boardInfo
    ) {
        Map<Long, EnumMap<SolutionEnum, List<Object>>> boardMap = new HashMap<>();

        boardInfo.forEach((boardId, data) -> {
            Map<SolutionEnum, List<Object>> processedData = processData(context, data);

            boardMap.put(boardId, new EnumMap<>(processedData));
        });

        context.setData(boardMap);
    }

    private <T> Map<SolutionEnum, List<Object>> processData(
        SolutionContext context,
        List<T> data
    ) {
        return context.solutionEnums.stream()
            .collect(Collectors.toMap(
                solutionEnum -> solutionEnum,
                solutionEnum -> data.stream()
                    .map(datum -> SolutionReflectionUtil.getField(datum, solutionEnum))
                    .collect(Collectors.toList())
            ));
    }

    private Map<Long, EnumMap<SolutionEnum, List<Object>>> createBoardMap(
        Long boardId,
        Map<SolutionEnum, List<Object>> processedData
    ) {
        EnumMap<SolutionEnum, List<Object>> enumMap = new EnumMap<>(SolutionEnum.class);

        processedData.forEach((solutionEnum, value) ->
            enumMap.computeIfAbsent(solutionEnum, k -> new ArrayList<>()).addAll(value)
        );

        Map<Long, EnumMap<SolutionEnum, List<Object>>> boardMap = new HashMap<>();
        boardMap.put(boardId, enumMap);

        return boardMap;
    }
}
