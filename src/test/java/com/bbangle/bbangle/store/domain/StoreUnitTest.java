package com.bbangle.bbangle.store.domain;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_DETAIL_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_EMAIL;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_IDENTIFIER;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_INTRODUCE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PHONE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PROFILE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_SUBPHONE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_DETAIL_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_EMAIL;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_IDENTIFIER;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_INTRODUCE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_PHONE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_PROFILE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_SUBPHONE;
import static com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture.NEW_STORE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("[단위 테스트] Store")
public class StoreUnitTest {

    @Nested
    @DisplayName("createForSeller() 테스트")
    class CreateForSellerTest {

        @Test
        @DisplayName("셀러 생성을 위한 스토어 객체 생성에 성공한다")
        void success_create_store_for_seller() {

            // given & when
            Store store = Store.createForSeller(
                DEFAULT_STORE_NAME, DEFAULT_PROFILE,
                DEFAULT_INTRODUCE, DEFAULT_IDENTIFIER,
                DEFAULT_PHONE, DEFAULT_SUBPHONE,
                DEFAULT_EMAIL,
                DEFAULT_ADDRESS, DEFAULT_DETAIL_ADDRESS
            );

            // then
            assertThat(store).isNotNull();
            assertThat(store.getName()).isEqualTo(DEFAULT_STORE_NAME);
            assertThat(store.getProfile()).isEqualTo(DEFAULT_PROFILE);
            assertThat(store.getIdentifier()).isEqualTo(DEFAULT_IDENTIFIER);
            assertThat(store.getIntroduce()).isEqualTo(DEFAULT_INTRODUCE);
            assertThat(store.getPhoneNumberVO().getPhoneNumber()).isEqualTo(DEFAULT_PHONE);
            assertThat(store.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo(DEFAULT_SUBPHONE);
            assertThat(store.getEmailVO().getEmail()).isEqualTo(DEFAULT_EMAIL);
            assertThat(store.getOriginAddressLine()).isEqualTo(DEFAULT_ADDRESS);
            assertThat(store.getOriginAddressDetail()).isEqualTo(DEFAULT_DETAIL_ADDRESS);
            assertThat(store.isDeleted()).isFalse();
        }

        @ParameterizedTest
        @DisplayName("스토어 생성 시 잘못된 전화번호로 인해 실패한다")
        @ValueSource(strings = {"12345", "abcd", "", "010-1234-5678"})
        void fail_create_store_with_invalid_phone(String invalidPhone) {
            // act & assert
            assertThatThrownBy(() -> Store.createForSeller(
                    DEFAULT_STORE_NAME, DEFAULT_PROFILE, DEFAULT_INTRODUCE, DEFAULT_IDENTIFIER,
                    invalidPhone,
                    DEFAULT_SUBPHONE, DEFAULT_EMAIL, DEFAULT_ADDRESS, DEFAULT_DETAIL_ADDRESS
                )
            ).isInstanceOf(BbangleException.class)
                .hasMessageContaining(BbangleErrorCode.INVALID_PHONE_NUMBER.getMessage());
        }

        @ParameterizedTest
        @DisplayName("스토어 생성 시 잘못된 서브 전화번호로 인해 실패한다")
        @ValueSource(strings = {"12345", "abcd", "", "010-1234-5678"})
        void fail_create_store_with_invalid_sub_phone(String invalidPhone) {
            // act & assert
            assertThatThrownBy(() -> Store.createForSeller(
                    DEFAULT_STORE_NAME, DEFAULT_PROFILE, DEFAULT_INTRODUCE, DEFAULT_IDENTIFIER, DEFAULT_PHONE,
                    invalidPhone,
                    DEFAULT_EMAIL, DEFAULT_ADDRESS, DEFAULT_DETAIL_ADDRESS
                )
            ).isInstanceOf(BbangleException.class)
                .hasMessageContaining(BbangleErrorCode.INVALID_PHONE_NUMBER.getMessage());
        }

        @ParameterizedTest
        @DisplayName("스토어 생성 시 잘못된 이메일 형식으로 인해 실패한다")
        @ValueSource(strings = {"test1234", "@gmail", "test@gmail", "test@.com", "test@com", ""})
        void fail_create_store_with_invalid_email(String invalidEmail) {
            // act & assert
            assertThatThrownBy(() -> Store.createForSeller(
                    DEFAULT_STORE_NAME, DEFAULT_PROFILE, DEFAULT_INTRODUCE, DEFAULT_IDENTIFIER, DEFAULT_PHONE, DEFAULT_SUBPHONE,
                    invalidEmail,
                    DEFAULT_ADDRESS, DEFAULT_DETAIL_ADDRESS
                )
            ).isInstanceOf(BbangleException.class)
                .hasMessageContaining(BbangleErrorCode.INVALID_EMAIL.getMessage());
        }
    }

    @Nested
    @DisplayName("updateDetail() 테스트")
    class UpdateDetailTest {

        static Stream<Arguments> updateParams() {
            return Stream.of(
                Arguments.of(null, null),
                Arguments.of(NEW_PROFILE, null),
                Arguments.of(NEW_PROFILE, NEW_SUBPHONE),
                Arguments.of(null, NEW_SUBPHONE)
            );
        }

        @ParameterizedTest
        @DisplayName("스토어 상세 정보 수정에 성공한다")
        @MethodSource("updateParams")
        void success_update_store(String newProfile, String newSubPhone) {

            // given
            Store store = StoreFixture.defaultStore();
            String profile = newProfile == null ? store.getProfile() : newProfile;

            // when
            store.updateDetail(newProfile, NEW_INTRODUCE, NEW_PHONE, newSubPhone, NEW_EMAIL, NEW_ADDRESS, NEW_DETAIL_ADDRESS);

            // then
            assertThat(store.getProfile()).isEqualTo(profile);
            assertThat(store.getIntroduce()).isEqualTo(NEW_INTRODUCE);
            assertThat(store.getPhoneNumberVO().getPhoneNumber()).isEqualTo(NEW_PHONE);
            assertThat(store.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo(newSubPhone);
            assertThat(store.getEmailVO().getEmail()).isEqualTo(NEW_EMAIL);
            assertThat(store.getOriginAddressLine()).isEqualTo(NEW_ADDRESS);
            assertThat(store.getOriginAddressDetail()).isEqualTo(NEW_DETAIL_ADDRESS);
        }

        @ParameterizedTest
        @DisplayName("스토어 상세 정보 수정 시 잘못된 전화번호로 인해 실패한다")
        @ValueSource(strings = {"12345", "abcd", "", "010-1234-5678"})
        void fail_update_store_with_invalid_phone(String invalidPhone) {

            // given
            Store store = StoreFixture.defaultStore();

            // act & assert
            assertThatThrownBy(() -> store.updateDetail(
                NEW_PROFILE, NEW_INTRODUCE,
                invalidPhone,
                NEW_SUBPHONE, NEW_EMAIL, NEW_ADDRESS, NEW_DETAIL_ADDRESS
                )
            ).isInstanceOf(BbangleException.class)
                .hasMessageContaining(BbangleErrorCode.INVALID_PHONE_NUMBER.getMessage());

            assertThat(store.getProfile()).isEqualTo(DEFAULT_PROFILE);
            assertThat(store.getIntroduce()).isEqualTo(DEFAULT_INTRODUCE);
            assertThat(store.getPhoneNumberVO().getPhoneNumber()).isEqualTo(DEFAULT_PHONE);
        }

        @ParameterizedTest
        @DisplayName("스토어 상세 정보 수정 시 잘못된 서브 전화번호로 인해 실패한다")
        @ValueSource(strings = {"12345", "abcd", "", "010-1234-5678"})
        void fail_update_store_with_invalid_sub_phone(String invalidPhone) {

            // given
            Store store = StoreFixture.defaultStore();

            // act & assert
            assertThatThrownBy(() -> store.updateDetail(
                NEW_PROFILE, NEW_INTRODUCE, NEW_PHONE,
                invalidPhone,
                NEW_EMAIL, NEW_ADDRESS, NEW_DETAIL_ADDRESS
                )
            ).isInstanceOf(BbangleException.class)
                .hasMessageContaining(BbangleErrorCode.INVALID_PHONE_NUMBER.getMessage());

            assertThat(store.getProfile()).isEqualTo(DEFAULT_PROFILE);
            assertThat(store.getIntroduce()).isEqualTo(DEFAULT_INTRODUCE);
            assertThat(store.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo(DEFAULT_SUBPHONE);
        }

        @ParameterizedTest
        @DisplayName("스토어 상세 정보 수정 시 잘못된 이메일 형식으로 인해 실패한다")
        @ValueSource(strings = {"test1234", "@gmail", "test@gmail", "test@.com", "test@com", ""})
        void fail_update_store_with_invalid_email(String invalidEmail) {

            // given
            Store store = StoreFixture.defaultStore();

            // act & assert
            assertThatThrownBy(() -> store.updateDetail(
                NEW_PROFILE, NEW_INTRODUCE, NEW_PHONE, NEW_SUBPHONE,
                invalidEmail,
                NEW_ADDRESS, NEW_DETAIL_ADDRESS
                )
            ).isInstanceOf(BbangleException.class)
                .hasMessageContaining(BbangleErrorCode.INVALID_EMAIL.getMessage());

            assertThat(store.getProfile()).isEqualTo(DEFAULT_PROFILE);
            assertThat(store.getIntroduce()).isEqualTo(DEFAULT_INTRODUCE);
            assertThat(store.getEmailVO().getEmail()).isEqualTo(DEFAULT_EMAIL);
        }

        @Test
        @DisplayName("스토어 상세 정보 수정 중 하나라도 유효하지 않으면 수정이 실패하고 상태는 유지된다.")
        void fail_update_store_any_invalid() {

            // given
            Store store = StoreFixture.defaultStore();
            String originalProfile = store.getProfile();

            // when & then
            assertThatThrownBy(() -> store.updateDetail(
                    NEW_PROFILE, NEW_INTRODUCE, NEW_PHONE, NEW_SUBPHONE,
                    null,
                    NEW_ADDRESS, NEW_DETAIL_ADDRESS
                )
            );

            assertThat(store.getProfile()).isEqualTo(originalProfile);
        }
    }

    @Nested
    @DisplayName("updateName() 테스트")
    class UpdateNameTest {

        @Test
        @DisplayName("이미 변경된 스토어명일 경우 변경에 실패한다")
        void fail_update_storeName() {

            // given
            Store store = StoreFixture.defaultStore();

            // when & then
            assertThatThrownBy(() -> store.updateName(NEW_STORE_NAME, "test"))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ALREADY_UPDATE_STORE_NAME);
                });
            assertThat(store.getName()).isNotEqualTo(NEW_STORE_NAME);
        }
    }

    @Nested
    @DisplayName("updateStoreForAdmin() 테스트")
    class UpdateStoreForAdminTest {

        static Stream<Arguments> updateParams() {
            return Stream.of(
                Arguments.of(null, null),
                Arguments.of(NEW_PROFILE, null),
                Arguments.of(NEW_PROFILE, NEW_SUBPHONE),
                Arguments.of(null, NEW_SUBPHONE)
            );
        }

        @ParameterizedTest
        @DisplayName("스토어 상세 정보 수정에 성공한다")
        @MethodSource("updateParams")
        void success_update_store(String newProfile, String newSubPhone) {

            // given
            Store store = StoreFixture.defaultStore();
            String profile = newProfile == null ? store.getProfile() : newProfile;

            // when
            store.updateStoreForAdmin(NEW_IDENTIFIER, newProfile, NEW_INTRODUCE, NEW_PHONE, newSubPhone, NEW_EMAIL, NEW_ADDRESS, NEW_DETAIL_ADDRESS);

            // then
            assertThat(store.getIdentifier()).isEqualTo(NEW_IDENTIFIER);
            assertThat(store.getProfile()).isEqualTo(profile);
            assertThat(store.getIntroduce()).isEqualTo(NEW_INTRODUCE);
            assertThat(store.getPhoneNumberVO().getPhoneNumber()).isEqualTo(NEW_PHONE);
            assertThat(store.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo(newSubPhone);
            assertThat(store.getEmailVO().getEmail()).isEqualTo(NEW_EMAIL);
            assertThat(store.getOriginAddressLine()).isEqualTo(NEW_ADDRESS);
            assertThat(store.getOriginAddressDetail()).isEqualTo(NEW_DETAIL_ADDRESS);
        }

        @Test
        @DisplayName("스토어 상세 정보 수정 시 프로필이 null이면 기존 프로필을 유지한다.")
        void success_update_store_nullProfile() {

            // given
            Store store = StoreFixture.defaultStore();

            // when
            store.updateStoreForAdmin(NEW_IDENTIFIER, null, NEW_INTRODUCE, NEW_PHONE, NEW_SUBPHONE, NEW_EMAIL, NEW_ADDRESS, NEW_DETAIL_ADDRESS);

            // then
            assertThat(store.getIdentifier()).isEqualTo(NEW_IDENTIFIER);
            assertThat(store.getProfile()).isEqualTo(DEFAULT_PROFILE);
            assertThat(store.getIntroduce()).isEqualTo(NEW_INTRODUCE);
            assertThat(store.getPhoneNumberVO().getPhoneNumber()).isEqualTo(NEW_PHONE);
            assertThat(store.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo(NEW_SUBPHONE);
            assertThat(store.getEmailVO().getEmail()).isEqualTo(NEW_EMAIL);
            assertThat(store.getOriginAddressLine()).isEqualTo(NEW_ADDRESS);
            assertThat(store.getOriginAddressDetail()).isEqualTo(NEW_DETAIL_ADDRESS);
        }

        @ParameterizedTest
        @DisplayName("스토어 상세 정보 수정 시 잘못된 전화번호로 인해 실패한다")
        @ValueSource(strings = {"12345", "abcd", "", "010-1234-5678"})
        void fail_update_store_with_invalid_phone(String invalidPhone) {

            // given
            Store store = StoreFixture.defaultStore();

            // act & assert
            assertThatThrownBy(() -> store.updateStoreForAdmin(
                NEW_IDENTIFIER, NEW_PROFILE, NEW_INTRODUCE,
                invalidPhone,
                NEW_SUBPHONE, NEW_EMAIL, NEW_ADDRESS, NEW_DETAIL_ADDRESS
                )
            ).isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_PHONE_NUMBER);
                });

            assertThat(store).usingRecursiveComparison().isEqualTo(StoreFixture.defaultStore());
        }

        @ParameterizedTest
        @DisplayName("스토어 상세 정보 수정 시 잘못된 서브 전화번호로 인해 실패한다")
        @ValueSource(strings = {"12345", "abcd", "", "010-1234-5678"})
        void fail_update_store_with_invalid_sub_phone(String invalidPhone) {

            // given
            Store store = StoreFixture.defaultStore();

            // act & assert
            assertThatThrownBy(() -> store.updateStoreForAdmin(
                NEW_IDENTIFIER, NEW_PROFILE, NEW_INTRODUCE, NEW_PHONE,
                invalidPhone,
                NEW_EMAIL, NEW_ADDRESS, NEW_DETAIL_ADDRESS
                )
            ).isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_PHONE_NUMBER);
                });

            assertThat(store).usingRecursiveComparison().isEqualTo(StoreFixture.defaultStore());
        }

        @ParameterizedTest
        @DisplayName("스토어 상세 정보 수정 시 잘못된 이메일 형식으로 인해 실패한다")
        @ValueSource(strings = {"test1234", "@gmail", "test@gmail", "test@.com", "test@com", ""})
        void fail_update_store_with_invalid_email(String invalidEmail) {

            // given
            Store store = StoreFixture.defaultStore();

            // act & assert
            assertThatThrownBy(() -> store.updateStoreForAdmin(
                NEW_IDENTIFIER, NEW_PROFILE, NEW_INTRODUCE, NEW_PHONE, NEW_SUBPHONE,
                invalidEmail,
                NEW_ADDRESS, NEW_DETAIL_ADDRESS
                )
            ).isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_EMAIL);
                });

            assertThat(store).usingRecursiveComparison().isEqualTo(StoreFixture.defaultStore());
        }

        @Test
        @DisplayName("스토어 상세 정보 수정 중 하나라도 유효하지 않으면 수정이 실패하고 상태는 유지된다.")
        void fail_update_store_any_invalid() {

            // given
            Store store = StoreFixture.defaultStore();

            // when & then
            assertThatThrownBy(() -> store.updateStoreForAdmin(
                NEW_IDENTIFIER, NEW_PROFILE, NEW_INTRODUCE, NEW_PHONE, NEW_SUBPHONE,
                null,
                NEW_ADDRESS, NEW_DETAIL_ADDRESS
                )
            ).isInstanceOf(BbangleException.class);

            assertThat(store).usingRecursiveComparison().isEqualTo(StoreFixture.defaultStore());
        }
    }

    @Nested
    @DisplayName("updateStoreWithName() 테스트")
    class UpdateStoreWithNameTest {

        static Stream<Arguments> updateParams() {
            return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", null),
                Arguments.of("   ", NEW_SUBPHONE),
                Arguments.of(NEW_STORE_NAME, NEW_SUBPHONE)
            );
        }

        @ParameterizedTest
        @DisplayName("스토어 상세 정보 수정에 성공한다")
        @MethodSource("updateParams")
        void success_update_store(String newName, String newSubPhone) {

            // given
            Store store = StoreFixture.defaultStore();
            String expectedName = (newName == null || newName.isBlank()) ? store.getName() : newName;

            // when
            store.updateStoreWithName(newName, NEW_IDENTIFIER, NEW_INTRODUCE, NEW_PHONE, newSubPhone, NEW_EMAIL, NEW_ADDRESS, NEW_DETAIL_ADDRESS);

            // then
            assertThat(store.getName()).isEqualTo(expectedName);
            assertThat(store.getIdentifier()).isEqualTo(NEW_IDENTIFIER);
            assertThat(store.getIntroduce()).isEqualTo(NEW_INTRODUCE);
            assertThat(store.getPhoneNumberVO().getPhoneNumber()).isEqualTo(NEW_PHONE);
            assertThat(store.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo(newSubPhone);
            assertThat(store.getEmailVO().getEmail()).isEqualTo(NEW_EMAIL);
            assertThat(store.getOriginAddressLine()).isEqualTo(NEW_ADDRESS);
            assertThat(store.getOriginAddressDetail()).isEqualTo(NEW_DETAIL_ADDRESS);
        }

        @ParameterizedTest
        @DisplayName("스토어 상세 정보 수정 시 잘못된 전화번호로 인해 실패한다")
        @ValueSource(strings = {"12345", "abcd", "", "010-1234-5678"})
        void fail_update_store_with_invalid_phone(String invalidPhone) {

            // given
            Store store = StoreFixture.defaultStore();

            // act & assert
            assertThatThrownBy(() -> store.updateStoreWithName(
                    NEW_STORE_NAME, NEW_IDENTIFIER, NEW_INTRODUCE,
                    invalidPhone,
                    NEW_SUBPHONE, NEW_EMAIL, NEW_ADDRESS, NEW_DETAIL_ADDRESS
                )
            ).isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_PHONE_NUMBER);
                });

            assertThat(store).usingRecursiveComparison().isEqualTo(StoreFixture.defaultStore());
        }

        @ParameterizedTest
        @DisplayName("스토어 상세 정보 수정 시 잘못된 서브 전화번호로 인해 실패한다")
        @ValueSource(strings = {"12345", "abcd", "", "010-1234-5678"})
        void fail_update_store_with_invalid_sub_phone(String invalidPhone) {

            // given
            Store store = StoreFixture.defaultStore();

            // act & assert
            assertThatThrownBy(() -> store.updateStoreWithName(
                    NEW_STORE_NAME, NEW_IDENTIFIER, NEW_INTRODUCE, NEW_PHONE,
                    invalidPhone,
                    NEW_EMAIL, NEW_ADDRESS, NEW_DETAIL_ADDRESS
                )
            ).isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_PHONE_NUMBER);
                });

            assertThat(store).usingRecursiveComparison().isEqualTo(StoreFixture.defaultStore());
        }

        @ParameterizedTest
        @DisplayName("스토어 상세 정보 수정 시 잘못된 이메일 형식으로 인해 실패한다")
        @ValueSource(strings = {"test1234", "@gmail", "test@gmail", "test@.com", "test@com", ""})
        void fail_update_store_with_invalid_email(String invalidEmail) {

            // given
            Store store = StoreFixture.defaultStore();

            // act & assert
            assertThatThrownBy(() -> store.updateStoreWithName(
                    NEW_STORE_NAME, NEW_IDENTIFIER, NEW_INTRODUCE, NEW_PHONE, NEW_SUBPHONE,
                    invalidEmail,
                    NEW_ADDRESS, NEW_DETAIL_ADDRESS
                )
            ).isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_EMAIL);
                });

            assertThat(store).usingRecursiveComparison().isEqualTo(StoreFixture.defaultStore());
        }

        @Test
        @DisplayName("스토어 상세 정보 수정 중 하나라도 유효하지 않으면 수정이 실패하고 상태는 유지된다.")
        void fail_update_store_any_invalid() {

            // given
            Store store = StoreFixture.defaultStore();

            // when & then
            assertThatThrownBy(() -> store.updateStoreWithName(
                    NEW_STORE_NAME, NEW_IDENTIFIER, NEW_INTRODUCE, NEW_PHONE, NEW_SUBPHONE,
                    null,
                    NEW_ADDRESS, NEW_DETAIL_ADDRESS
                )
            ).isInstanceOf(BbangleException.class);

            assertThat(store).usingRecursiveComparison().isEqualTo(StoreFixture.defaultStore());
        }
    }
}
