package com.bbangle.bbangle.linktracking.customer.controller.swagger;

import com.bbangle.bbangle.linktracking.domain.LinkChannel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

@Tag(name = "LinkTracking", description = "외부 유입 링크 추적")
public interface LinkTrackingApi {

    @Operation(
        summary = "추적 링크 리다이렉트",
        description = "조회수를 기록한 뒤 실제 목적지로 302 리다이렉트"
    )
    ResponseEntity<Void> redirect(
        @Parameter(description = "유입 채널", example = "INSTAGRAM") LinkChannel channel,
        @Parameter(hidden = true) HttpServletRequest request
    );
}
