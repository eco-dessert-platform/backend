package com.bbangle.bbangle.linktracking.customer.controller.mapper;

import com.bbangle.bbangle.linktracking.customer.service.command.LinkTrackingCommand;
import com.bbangle.bbangle.linktracking.domain.LinkChannel;
import com.bbangle.bbangle.util.VisitorFingerprintUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.validation.annotation.Validated;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
@Validated
public interface LinkTrackingCommandMapper {

    @Valid
    @Mapping(target = "channel", source = "channel")
    @Mapping(target = "ipAddress", source = "request", qualifiedByName = "extractIp")
    @Mapping(target = "userAgent", source = "request", qualifiedByName = "extractUserAgent")
    @Mapping(target = "referer", source = "request", qualifiedByName = "extractReferer")
    LinkTrackingCommand.Visit toVisit(LinkChannel channel, HttpServletRequest request);

    @Named("extractIp")
    default String extractIp(HttpServletRequest request) {
        return VisitorFingerprintUtils.extractIp(request);
    }

    @Named("extractUserAgent")
    default String extractUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    @Named("extractReferer")
    default String extractReferer(HttpServletRequest request) {
        return request.getHeader("Referer");
    }
}
