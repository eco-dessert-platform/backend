package com.bbangle.bbangle.member.customer.dto;

public record MemberAssignResponse(
    Boolean isFullyAssigned,
    Boolean isPreferenceAssigned,
    Boolean isSurveyed
) {

}
