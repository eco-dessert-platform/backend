package com.bbangle.bbangle.store.domain;


import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_DETAIL_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_EMAIL;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_IDENTIFIER;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_INTRODUCE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_NAME;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PHONE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PROFILE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_SUBPHONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("[단위 테스트] Store")
public class StoreUnitTest {

    private final String newName = "newName";
    private final String newProfile = "test/s3/newProfile";
    private final String newIntroduce = "newIntroduce";
    private final String newPhone = "01011112222";
    private final String newSubPhone = "01099998888";
    private final String newEmail = "123@temp.com";
    private final String newAddress = "서울";
    private final String newDetailAddress = "123동";

    @Test
    @DisplayName("셀러 생성을 위한 스토어 객체 생성에 성공한다")
    void success_create_store_for_seller() {

        // given & when
        Store store = Store.createForSeller(
            DEFAULT_NAME, DEFAULT_PROFILE,
            DEFAULT_IDENTIFIER, DEFAULT_INTRODUCE,
            DEFAULT_PHONE, DEFAULT_SUBPHONE,
            DEFAULT_EMAIL,
            DEFAULT_ADDRESS, DEFAULT_DETAIL_ADDRESS
        );

        // then
        assertThat(store).isNotNull();
        assertThat(store.getName()).isEqualTo(DEFAULT_NAME);
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
            DEFAULT_NAME, DEFAULT_PROFILE, DEFAULT_IDENTIFIER, DEFAULT_INTRODUCE,
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
            DEFAULT_NAME, DEFAULT_PROFILE, DEFAULT_IDENTIFIER, DEFAULT_INTRODUCE, DEFAULT_PHONE,
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
            DEFAULT_NAME, DEFAULT_PROFILE, DEFAULT_IDENTIFIER, DEFAULT_INTRODUCE, DEFAULT_PHONE, DEFAULT_SUBPHONE,
            invalidEmail,
            DEFAULT_ADDRESS, DEFAULT_DETAIL_ADDRESS
            )
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_EMAIL.getMessage());
    }

    @Test
    @DisplayName("스토어 상세 정보 수정에 성공한다")
    void success_update_store() {

        // given
        Store store = StoreFixture.defaultStore();

        // when
        store.updateDetail(newProfile, newIntroduce, newPhone, newSubPhone, newEmail, newAddress, newDetailAddress);

        // then
        assertThat(store.getProfile()).isEqualTo(newProfile);
        assertThat(store.getIntroduce()).isEqualTo(newIntroduce);
        assertThat(store.getPhoneNumberVO().getPhoneNumber()).isEqualTo(newPhone);
        assertThat(store.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo(newSubPhone);
        assertThat(store.getEmailVO().getEmail()).isEqualTo(newEmail);
        assertThat(store.getOriginAddressLine()).isEqualTo(newAddress);
        assertThat(store.getOriginAddressDetail()).isEqualTo(newDetailAddress);
    }

    @ParameterizedTest
    @DisplayName("스토어 상세 정보 수정 시 잘못된 전화번호로 인해 실패한다")
    @ValueSource(strings = {"12345", "abcd", "", "010-1234-5678"})
    void fail_update_store_with_invalid_phone(String invalidPhone) {

        // given
        Store store = StoreFixture.defaultStore();

        // act & assert
        assertThatThrownBy(() -> store.updateDetail(
            newProfile, newIntroduce,
            invalidPhone,
            newSubPhone, newEmail, newAddress, newDetailAddress
            )
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_PHONE_NUMBER.getMessage());
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
            newProfile, newIntroduce, newPhone,
            invalidPhone,
            newEmail, newAddress, newDetailAddress
            )
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_PHONE_NUMBER.getMessage());
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
            newProfile, newIntroduce, newPhone, newSubPhone,
            invalidEmail,
            newAddress, newDetailAddress
            )
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_EMAIL.getMessage());
        assertThat(store.getEmailVO().getEmail()).isEqualTo(DEFAULT_EMAIL);
    }
}
