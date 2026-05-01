package com.bbangle.bbangle.linktracking.customer.controller;

import com.bbangle.bbangle.linktracking.customer.controller.mapper.LinkTrackingCommandMapper;
import com.bbangle.bbangle.linktracking.customer.controller.swagger.LinkTrackingApi;
import com.bbangle.bbangle.linktracking.customer.facade.LinkTrackingFacade;
import com.bbangle.bbangle.linktracking.customer.service.command.LinkTrackingCommand;
import com.bbangle.bbangle.linktracking.domain.LinkChannel;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/link")
@RequiredArgsConstructor
public class LinkTrackingController implements LinkTrackingApi {

    private final LinkTrackingFacade linkTrackingFacade;
    private final LinkTrackingCommandMapper linkTrackingCommandMapper;

    @GetMapping("/{channel}")
    public ResponseEntity<Void> redirect(
        @PathVariable("channel") LinkChannel channel,
        HttpServletRequest request
    ) {
        LinkTrackingCommand.Visit command = linkTrackingCommandMapper.toVisit(channel, request);
        String destinationUrl = linkTrackingFacade.resolveAndRecordVisit(command);

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(destinationUrl))
            .build();
    }
}
