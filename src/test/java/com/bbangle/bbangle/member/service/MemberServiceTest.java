package com.bbangle.bbangle.member.service;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.dto.MemberAssignResponse;
import com.bbangle.bbangle.member.dto.MemberInfoRequest;
import com.bbangle.bbangle.preference.domain.PreferenceType;
import com.bbangle.bbangle.preference.dto.PreferenceSelectRequest;
import com.bbangle.bbangle.preference.service.PreferenceService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

class MemberServiceTest extends AbstractIntegrationTest {

    private static final MultipartFile NULL_FILE = null;

    @Autowired
    PreferenceService preferenceService;

    Member member;

    @BeforeEach
    void setup() {
        member = memberService.getFirstJoinedMember(MemberFixture.createKakaoMember());
    }

    @Test
    @DisplayName("정상적으로 멤버가 약관에 동의하고 선호도를 기록했는지 확인할 수 있다.")
    void getIsFullyAssigned() {
        //given, when
        MemberAssignResponse isAssigned = memberService.getIsAssigned(member.getId());

        //then
        Assertions.assertThat(isAssigned.isFullyAssigned())
            .isFalse();
        Assertions.assertThat(isAssigned.isPreferenceAssigned())
            .isFalse();
    }

    @Test
    @DisplayName("취향을 등록한 경우에도 정상적으로 멤버가 약관에 동의하고 선호도를 기록했는지 확인할 수 있다.")
    void getIsFullyAssignedWithPreferenceTrue() {
        //given, when
        preferenceService.register(new PreferenceSelectRequest(PreferenceType.DIET),
            member.getId());
        MemberAssignResponse isAssigned = memberService.getIsAssigned(member.getId());

        //then
        Assertions.assertThat(isAssigned.isFullyAssigned())
            .isFalse();
        Assertions.assertThat(isAssigned.isPreferenceAssigned())
            .isTrue();
    }

    @Test
    @DisplayName("취향을 등록한 경우에도 정상적으로 멤버가 약관에 동의하고 선호도를 기록했는지 확인할 수 있다.")
    void getIsFullyAssignedWithAssignment() {
        //given, when
        MemberInfoRequest request = new MemberInfoRequest(
            "nickname",
            "01000000000",
            "1999-01-01",
            true,
            true,
            true
        );

        memberService.updateMemberInfo(request, member.getId(), NULL_FILE);
        MemberAssignResponse isAssigned = memberService.getIsAssigned(member.getId());

        //then
        Assertions.assertThat(isAssigned.isFullyAssigned())
            .isTrue();
        Assertions.assertThat(isAssigned.isPreferenceAssigned())
            .isFalse();
    }

}
