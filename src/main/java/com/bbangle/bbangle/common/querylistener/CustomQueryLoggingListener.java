package com.bbangle.bbangle.common.querylistener;

import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;

import java.util.List;

@Slf4j
public class CustomQueryLoggingListener implements QueryExecutionListener {

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryList) {

    }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryList) {
        QueryTimerContext.addTime(execInfo.getElapsedTime());
        long time = execInfo.getElapsedTime();
        boolean success = execInfo.isSuccess();

        for (QueryInfo query : queryList) {
            String sql = query.getQuery();

            List<List<String>> allParams = query.getParametersList().stream()
                    .map(params -> params.stream()
                            .map(p -> String.valueOf(p.getArgs()[1]))
                            .toList())
                    .toList();

            log.debug("""
                    
                    - 쿼리문      : {}
                    - 쿼리파라미터 : {}
                    - 성공여부    : {}
                    - 처리시간    : {}ms""", sql, allParams, success, time);
        }
    }
}