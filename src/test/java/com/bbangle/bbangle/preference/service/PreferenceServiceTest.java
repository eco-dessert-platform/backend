package com.bbangle.bbangle.preference.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.member.service.MemberService;
import com.bbangle.bbangle.preference.domain.Preference;
import com.bbangle.bbangle.preference.domain.PreferenceType;
import com.bbangle.bbangle.preference.dto.MemberPreferenceResponse;
import com.bbangle.bbangle.preference.dto.PreferenceSelectRequest;
import com.bbangle.bbangle.preference.dto.PreferenceUpdateRequest;
import com.bbangle.bbangle.preference.repository.MemberPreferenceRepository;
import com.bbangle.bbangle.preference.repository.PreferenceRepository;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import com.bbangle.bbangle.wishlist.repository.WishListFolderRepository;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PreferenceServiceTest extends AbstractIntegrationTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PreferenceService preferenceService;

    @Autowired
    private PreferenceRepository preferenceRepository;

    Member member;

    @BeforeEach
    public void setup() {
        member = MemberFixture.createKakaoMember();
        member = memberService.getFirstJoinedMember(member);
    }

    @Nested
    @DisplayName("취향 저장 테스트")
    class SavePreference {

        @ParameterizedTest
        @EnumSource(PreferenceType.class)
        @DisplayName("멤버는 정상적으로 취향 등록에 성공한다")
        public void savePreference(PreferenceType preferenceType) throws Exception {
            //given
            PreferenceSelectRequest request = new PreferenceSelectRequest(
                preferenceType);

            //when, then
            Assertions.assertDoesNotThrow(
                () -> preferenceService.register(request, member.getId()));
        }

        @Test
        @DisplayName("이미 취향을 등록한 사람은 새로 등록할 수는 없다")
        public void cannotSaveTwice() throws Exception {
            //given
            PreferenceSelectRequest request = new PreferenceSelectRequest(
                PreferenceType.DIET);
            preferenceService.register(request, member.getId());

            //when, then
            assertThatThrownBy(() -> preferenceService.register(request, member.getId()))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.PREFERENCE_ALREADY_ASSIGNED.getMessage());
        }

    }

    @Nested
    @DisplayName("취향 조회 테스트")
    class GetPreference {

        @ParameterizedTest
        @EnumSource(PreferenceType.class)
        @DisplayName("멤버는 정상적으로 등록된 취향을 조회한다")
        void getPreference(PreferenceType preferenceType) {
            //given
            PreferenceSelectRequest request = new PreferenceSelectRequest(
                preferenceType);

            //when, then
            preferenceService.register(request, member.getId());
            MemberPreferenceResponse preference = preferenceService.getPreference(member.getId());

            assertThat(preference.preferenceType()).isEqualTo(preferenceType);
        }

        @Test
        @DisplayName("취향을 등록하지 않은 멤버는 취향을 조회할 수 없다")
        void cannotUpdatePreferenceWithOutSavedPreference() {
            //given, when, then
            assertThatThrownBy(
                () -> preferenceService.getPreference(member.getId()))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.MEMBER_PREFERENCE_NOT_FOUND.getMessage());
        }

    }

    @Nested
    @DisplayName("취향 업데이트 테스트")
    class UpdatePreference {

        @Test
        @DisplayName("멤버는 정상적으로 등록된 취향을 조회한다")
        void updatePreference() {
            //given
            PreferenceSelectRequest request = new PreferenceSelectRequest(
                PreferenceType.DIET);
            List<Preference> all = preferenceRepository.findAll();

            preferenceService.register(request, member.getId());

            //when,
            PreferenceUpdateRequest updateRequest = new PreferenceUpdateRequest(
                PreferenceType.CONSTITUTION);
            preferenceService.update(updateRequest, member.getId());
            MemberPreferenceResponse preference = preferenceService.getPreference(member.getId());

            // then
            assertThat(preference.preferenceType()).isEqualTo(updateRequest.preferenceType());
        }

        @Test
        @DisplayName("취향을 등록하지 않은 멤버는 취향을 업데이트할 수 없다")
        void cannotUpdatePreferenceWithOutSavedPreference() {
            //given
            PreferenceSelectRequest request = new PreferenceSelectRequest(
                PreferenceType.DIET);
            preferenceService.register(request, member.getId());
            Member fixtureMember = MemberFixture.createKakaoMember();
            Member unSavePreferenceMember = memberService.getFirstJoinedMember(fixtureMember);

            //when, then
            PreferenceUpdateRequest updateRequest = new PreferenceUpdateRequest(
                PreferenceType.CONSTITUTION);
            assertThatThrownBy(
                () -> preferenceService.update(updateRequest, unSavePreferenceMember.getId()))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.MEMBER_PREFERENCE_NOT_FOUND.getMessage());
        }

    }

}
