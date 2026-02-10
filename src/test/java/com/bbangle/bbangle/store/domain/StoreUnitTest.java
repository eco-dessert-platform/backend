package com.bbangle.bbangle.store.domain;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("[단위 테스트] Store")
public class StoreUnitTest {

    private final String name = "test";
    private final String profile = "test/s3/seller";
    private final String introduce = "testIntroduce";
    private final String phone = "01012346789";
    private final String subPhone = "01098765432";
    private final String email = "test1234@gmail.com";
    private final String address = "경기도 수원시 팔달구";
    private final String detailAddress = "화성행궁 12번지";

    private final String newName = "newName";
    private final String newProfile = "test/s3/newProfile";
    private final String newIntroduce = "newIntroduce";
    private final String newPhone = "01011112222";
    private final String newSubPhone = "01099998888";
    private final String newEmail = "123@temp.com";
    private final String newAddress = "서울";
    private final String newDetailAddress = "123동";

    private Store createDefaultStore() {
        return Store.createForSeller(name, profile, introduce, phone, subPhone, email, address, detailAddress);
    }

    @Test
    @DisplayName("셀러 생성을 위한 스토어 객체 생성에 성공한다")
    void success_create_store_for_seller() {

        // given & when
        Store store = createDefaultStore();

        // then
        assertThat(store).isNotNull();
        assertThat(store.getName()).isEqualTo(name);
        assertThat(store.getProfile()).isEqualTo(profile);
        assertThat(store.getIntroduce()).isEqualTo(introduce);
        assertThat(store.getPhoneNumberVO().getPhoneNumber()).isEqualTo(phone);
        assertThat(store.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo(subPhone);
        assertThat(store.getEmailVO().getEmail()).isEqualTo(email);
        assertThat(store.getOriginAddressLine()).isEqualTo(address);
        assertThat(store.getOriginAddressDetail()).isEqualTo(detailAddress);
        assertThat(store.isDeleted()).isFalse();
        assertThat(store.getStatus()).isEqualTo(StoreStatus.NONE);
    }

    @ParameterizedTest
    @DisplayName("스토어 생성 시 잘못된 전화번호로 인해 실패한다")
    @ValueSource(strings = {"12345", "abcd", "", "010-1234-5678"})
    void fail_create_store_with_invalid_phone(String invalidPhone) {
        // act & assert
        assertThatThrownBy(() -> Store.createForSeller(name, profile, introduce, invalidPhone, subPhone, email, address, detailAddress)
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_PHONE_NUMBER.getMessage());
    }

    @ParameterizedTest
    @DisplayName("스토어 생성 시 잘못된 서브 전화번호로 인해 실패한다")
    @ValueSource(strings = {"12345", "abcd", "", "010-1234-5678"})
    void fail_create_store_with_invalid_sub_phone(String invalidPhone) {
        // act & assert
        assertThatThrownBy(() -> Store.createForSeller(name, profile, introduce, phone, invalidPhone, email, address, detailAddress)
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_PHONE_NUMBER.getMessage());
    }

    @ParameterizedTest
    @DisplayName("스토어 생성 시 잘못된 이메일 형식으로 인해 실패한다")
    @ValueSource(strings = {"test1234", "@gmail", "test@gmail", "test@.com", "test@com", ""})
    void fail_create_store_with_invalid_email(String invalidEmail) {
        // act & assert
        assertThatThrownBy(() -> Store.createForSeller(name, profile, introduce, phone, subPhone, invalidEmail, address, detailAddress)
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_EMAIL.getMessage());
    }

    @Test
    @DisplayName("스토어 상세 정보 수정에 성공한다")
    void success_update_store() {

        // given
        Store store = createDefaultStore();

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
        Store store = createDefaultStore();

        // act & assert
        assertThatThrownBy(() -> store.updateDetail(newProfile, newIntroduce, invalidPhone, newSubPhone, newEmail, newAddress, newDetailAddress)
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_PHONE_NUMBER.getMessage());
        assertThat(store.getPhoneNumberVO().getPhoneNumber()).isEqualTo(phone);
    }

    @ParameterizedTest
    @DisplayName("스토어 상세 정보 수정 시 잘못된 서브 전화번호로 인해 실패한다")
    @ValueSource(strings = {"12345", "abcd", "", "010-1234-5678"})
    void fail_update_store_with_invalid_sub_phone(String invalidPhone) {

        // given
        Store store = createDefaultStore();

        // act & assert
        assertThatThrownBy(() -> store.updateDetail(newProfile, newIntroduce, newPhone, invalidPhone, newEmail, newAddress, newDetailAddress)
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_PHONE_NUMBER.getMessage());
        assertThat(store.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo(subPhone);
    }

    @ParameterizedTest
    @DisplayName("스토어 상세 정보 수정 시 잘못된 이메일 형식으로 인해 실패한다")
    @ValueSource(strings = {"test1234", "@gmail", "test@gmail", "test@.com", "test@com", ""})
    void fail_update_store_with_invalid_email(String invalidEmail) {

        // given
        Store store = createDefaultStore();

        // act & assert
        assertThatThrownBy(() -> store.updateDetail(newProfile, newIntroduce, newPhone, newSubPhone, invalidEmail, newAddress, newDetailAddress)
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_EMAIL.getMessage());
        assertThat(store.getEmailVO().getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("스토어 상태 변경에 성공한다.")
    void success_change_status() {

        // given
        Store store = createDefaultStore();

        // when
        store.changeStatus(StoreStatus.RESERVED);

        // then
        assertThat(store.getStatus()).isEqualTo(StoreStatus.RESERVED);
    }
}
