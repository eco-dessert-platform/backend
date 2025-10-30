package com.bbangle.bbangle.member.customer.controller.mapper;

import com.bbangle.bbangle.member.customer.controller.dto.ProfileResponse;
import com.bbangle.bbangle.member.customer.service.dto.ProfileInfo;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProfileMapper {

    ProfileResponse.DefaultProfile toDefaultProfileInfo(ProfileInfo.DefaultProfile info);

}
