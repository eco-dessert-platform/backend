package com.bbangle.bbangle.member.customer.service.mapper;

import com.bbangle.bbangle.member.customer.service.dto.ProfileInfo;
import com.bbangle.bbangle.member.domain.Member;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProfileInfoMapper {

    @Mapping(target = "profileImg", source = "profile")
    @Mapping(target = "birthDate", source = "birth")
    ProfileInfo.DefaultProfile toDefaultProfileInfo(Member member);
}
