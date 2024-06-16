package com.bbangle.bbangle.common.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
public record MessageDto(
    String message,
    @JsonInclude(NON_EMPTY)
    Boolean isValid
) {

}

