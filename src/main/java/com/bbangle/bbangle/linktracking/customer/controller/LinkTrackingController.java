package com.bbangle.bbangle.linktracking.customer.controller;

import com.bbangle.bbangle.linktracking.customer.service.command.LinkTrackingCommand;
import com.bbangle.bbangle.linktracking.customer.facade.LinkTrackingFacade;
import com.bbangle.bbangle.linktracking.customer.controller.mapper.LinkTrackingCommandMapper;
import com.bbangle.bbangle.linktracking.domain.LinkChannel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "LinkTracking", description = "외부 유입 링크 추적")
@RestController
@RequestMapping("/api/v1/link")
@RequiredArgsConstructor
public class LinkTrackingController {

    private final LinkTrackingFacade linkTrackingFacade;
    private final LinkTrackingCommandMapper linkTrackingCommandMapper;

    @Operation(summary = "추적 링크 리다이렉트", description = "조회수를 기록한 뒤 실제 목적지로 302 리다이렉트")
    @GetMapping("/{channel}")
    public ResponseEntity<Void> redirect(
        @Parameter(description = "유입 채널", example = "INSTAGRAM")
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
