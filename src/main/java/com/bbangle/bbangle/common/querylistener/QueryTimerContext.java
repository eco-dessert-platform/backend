package com.bbangle.bbangle.common.querylistener;

public class QueryTimerContext {

    private static final ThreadLocal<Long> totalTime = ThreadLocal.withInitial(() -> 0L);

    public static void addTime(long millis) {
        totalTime.set(totalTime.get() + millis);
    }

    public static long getTotalTime() {
        return totalTime.get();
    }

    public static void clear() {
        totalTime.remove();
    }

}
