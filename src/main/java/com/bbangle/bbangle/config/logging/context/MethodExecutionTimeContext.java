package com.bbangle.bbangle.config.logging.context;

import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;

/**
 * 메서드 실행 시간 로그 처리
 */
@NoArgsConstructor
public class MethodExecutionTimeContext {

    // API당 계층별 메서드 실행 시간을 기록할 스레드 생성
    private static final ThreadLocal<List<String>> LOGS = ThreadLocal.withInitial(ArrayList::new);

    // 스레드에 메서드 실행 시간 추가
    public static void add(String log) {
        LOGS.get().add(log);
    }

    /**
     * 계층별 실행 시간 로그 형식
     * @return 기록된 로그가 없으면 null (prod처럼 AOP가 동작하지 않는 환경 포함).
     *         응답 로그 DTO는 이 값이 null이면 "계층별 실행시간" 섹션 자체를 생략한다.
     */
    public static String getFormattedLog() {
        List<String> logs = LOGS.get();
        if (logs.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (String l : logs) {
            sb.append("    ").append(l).append("\n");
        }

        return sb.toString().stripTrailing();
    }

    public static void clear() {
        LOGS.remove();
    }
}
