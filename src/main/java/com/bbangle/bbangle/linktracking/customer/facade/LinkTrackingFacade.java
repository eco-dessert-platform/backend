package com.bbangle.bbangle.linktracking.customer.facade;

import com.bbangle.bbangle.linktracking.customer.service.command.LinkTrackingCommand;
import com.bbangle.bbangle.linktracking.customer.service.LinkTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LinkTrackingFacade {

    private final LinkTrackingService linkTrackingService;

    public String resolveAndRecordVisit(LinkTrackingCommand.Visit command) {
        return linkTrackingService.resolveAndRecordVisit(command);
    }
}
