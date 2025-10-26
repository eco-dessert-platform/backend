package com.bbangle.bbangle.member.service;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.fixturemonkey.FixtureMonkeyConfig;
import com.bbangle.bbangle.member.customer.dto.MemberAssignResponse;
import com.bbangle.bbangle.member.customer.dto.MemberIdWithRoleDto;
import com.bbangle.bbangle.member.customer.dto.MemberInfoRequest;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.preference.domain.PreferenceType;
import com.bbangle.bbangle.preference.dto.PreferenceSelectRequest;
import com.bbangle.bbangle.preference.service.PreferenceService;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
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

    MemberIdWithRoleDto memberIdWithRoleDto;
    Member member;

    @BeforeEach
    void setup() {
        member = FixtureMonkeyConfig.fixtureMonkey.giveMeBuilder(Member.class)
            .set("provider", OauthServerType.KAKAO)
            .sample();
        member = memberService.getFirstJoinedMember(member);
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
        MemberInfoRequest request = FixtureMonkeyConfig.fixtureMonkey.giveMeBuilder(
                MemberInfoRequest.class)
            .set("isAllowingMarketing", true)
            .set("isPersonalInfoConsented", true)
            .set("isTermsOfServiceAccepted", true)
            .set("birthDate", "2024-11-13")
            .sample();

        memberService.updateMemberInfo(request, member.getId(), NULL_FILE);
        MemberAssignResponse isAssigned = memberService.getIsAssigned(member.getId());

        //then
        Assertions.assertThat(isAssigned.isFullyAssigned())
            .isTrue();
        Assertions.assertThat(isAssigned.isPreferenceAssigned())
            .isFalse();
    }

    @Test
    @DisplayName("멤버는 정상적으로 탈퇴가 가능하다")
    void getWithdrawSuccess() {
        //given
        memberService.deleteMember(member.getId());

        //when
        Member deletedMember = memberRepository.findById(member.getId()).orElseThrow();

        //then
        Assertions.assertThat(deletedMember.isDeleted()).isTrue();
        Assertions.assertThat(deletedMember.getEmail()).isEqualTo("-");
        Assertions.assertThat(deletedMember.getPhone()).isEqualTo("-");
        Assertions.assertThat(deletedMember.getName()).isEqualTo("-");
        Assertions.assertThat(deletedMember.getNickname()).isEqualTo("-");
        Assertions.assertThat(deletedMember.getBirth()).isEqualTo("-");
        Assertions.assertThat(deletedMember.getProviderId()).isEqualTo("-");
    }

    @Test
    @DisplayName("탈퇴 후 새로 가입한 멤버는 다른 멤버로 취급한다.")
    void reJoinMemberIsNotSameWithBefore() {
        //given
        String name = member.getName();
        String nickName = member.getNickname();
        String email = member.getEmail();
        memberService.deleteMember(member.getId());
        Member newMember = FixtureMonkeyConfig.fixtureMonkey.giveMeBuilder(Member.class)
            .set("name", name)
            .set("nickName", nickName)
            .set("email", email)
            .sample();

        //when
        Member joinedMember = memberService.getFirstJoinedMember(newMember);

        //then
        Assertions.assertThat(member.getId()).isNotEqualTo(joinedMember.getId());
    }

}

