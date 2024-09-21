package com.bbangle.bbangle.member.service;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.domain.Sex;
import com.bbangle.bbangle.member.dto.MemberAssignResponse;
import com.bbangle.bbangle.member.dto.MemberInfoRequest;
import com.bbangle.bbangle.preference.domain.PreferenceType;
import com.bbangle.bbangle.preference.dto.PreferenceSelectRequest;
import com.bbangle.bbangle.preference.service.PreferenceService;
import java.util.Optional;
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

    Member kakaoMember;
    Long memberId;

    @BeforeEach
    void setup() {
        kakaoMember = MemberFixture.createKakaoMember();
        memberId = memberService.getFirstJoinedMember(kakaoMember);
    }

    @Test
    @DisplayName("정상적으로 멤버가 약관에 동의하고 선호도를 기록했는지 확인할 수 있다.")
    void getIsFullyAssigned() {
        //given, when
        MemberAssignResponse isAssigned = memberService.getIsAssigned(memberId);

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
            memberId);
        MemberAssignResponse isAssigned = memberService.getIsAssigned(memberId);

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
            "1999-01-01",
            Sex.MAN,
            true,
            true,
            true
        );

        memberService.updateMemberInfo(request, memberId, NULL_FILE);
        MemberAssignResponse isAssigned = memberService.getIsAssigned(memberId);

        //then
        Assertions.assertThat(isAssigned.isFullyAssigned())
            .isTrue();
        Assertions.assertThat(isAssigned.isPreferenceAssigned())
            .isFalse();
    }

    @Test
    @DisplayName("멤버는 정상적으로 탈퇴가 가능하다")
    void getWithdrawSuccess(){
        //given
        memberService.deleteMember(memberId);

        //when
        Member deletedMember = memberRepository.findById(memberId).orElseThrow();

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
    void reJoinMemberIsNotSameWithBefore(){
        //given
        memberService.deleteMember(memberId);
        Member sameMemberQuit = MemberFixture.sameKaKaoMember(kakaoMember);

        //when
        Long rejoinedId = memberService.getFirstJoinedMember(sameMemberQuit);

        //then
        Assertions.assertThat(memberId).isNotEqualTo(rejoinedId);
    }

}
