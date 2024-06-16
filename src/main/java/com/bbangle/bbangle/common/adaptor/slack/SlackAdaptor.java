package com.bbangle.bbangle.common.adaptor.slack;

import jakarta.servlet.http.HttpServletRequest;

public interface SlackAdaptor {

    void sendAlert(HttpServletRequest httpServletRequest, Throwable t);
}
