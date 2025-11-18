package com.bbangle.bbangle.seller.domain;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.store.domain.Store;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SellerUnitTest {

    @Mock
    private Store store;

    @Test
    @DisplayName("판매자 정보 생성에 성공한다")
    void success_create_seller() {
        // arrange
        String phone = "01012346789";
        String email = "test1234@gmail.com";
        String address = "경기도 수원시 팔달구";
        String detailAddress = "화성행궁 12번지";
        String profile = "test/s3/seller";


        // act
        Seller seller = Seller.create(phone, phone, email,
            address, detailAddress, profile, CertificationStatus.APPROVED, store);

        // assert
        assertThat(seller).isNotNull();
        assertThat(seller.getPhone()).isEqualTo(phone);
        assertThat(seller.getSubPhone()).isEqualTo(phone);
        assertThat(seller.getEmail()).isEqualTo(email);
        assertThat(seller.getOriginAddressLine()).isEqualTo(address);
        assertThat(seller.getOriginAddressDetail()).isEqualTo(detailAddress);
        assertThat(seller.getProfile()).isEqualTo(profile);
        assertThat(seller.getCertificationStatus()).isEqualTo(CertificationStatus.APPROVED);
        assertThat(seller.getStore()).isEqualTo(store);

    }

    @ParameterizedTest
    @DisplayName("판매자 정보 생성 시 잘못된 전화번호로 인해 실패한다")
    @ValueSource(strings = {"12345", "abcd", "", "010-1234-5678"})
    void fail_create_seller_with_invalid_phone(String invalidPhone) {
         // act & assert
        assertThatThrownBy(() -> Seller.create(invalidPhone, "01012346789", "test1234@gmail.com",
            "경기도 수원시 팔달구","화성행궁 12번지", "test/s3/seller", CertificationStatus.APPROVED, store)
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_PHONE_NUMBER.getMessage());
    }

    @ParameterizedTest
    @DisplayName("판매자 정보 생성 시 잘못된 서브 전화번호로 인해 실패한다")
    @ValueSource(strings = {"12345", "abcd", "", "010-1234-5678"})
    void fail_create_seller_with_invalid_sub_phone(String invalidPhone) {
        // arrange
        // act & assert
        assertThatThrownBy(() -> Seller.create("01012346789", invalidPhone, "test1234@gmail.com",
            "경기도 수원시 팔달구","화성행궁 12번지", "test/s3/seller", CertificationStatus.APPROVED , store)
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_PHONE_NUMBER.getMessage());
    }


    @ParameterizedTest
    @DisplayName("판매자 정보 생성 시 잘못된 이메일 형식으로 인해 실패한다")
    @ValueSource(strings = {"test1234", "@gmail", "test@gmail", "test@.com", "test@com", ""})
    void fail_create_seller_with_invalid_email(String invalidEmail) {
        // act & assert
        assertThatThrownBy(() -> Seller.create("01012346789", "01012346789", invalidEmail,
            "경기도 수원시 팔달구","화성행궁 12번지", "test/s3/seller", CertificationStatus.APPROVED, store)
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_EMAIL.getMessage());
    }

    @Test
    @DisplayName("판매자 정보 생성시 비어 있는 주소로 인해 실패한다")
    void fail_create_seller_with_invalid_address() {
        assertThatThrownBy(() -> Seller.create("01012346789", "01012346789", "test1234@gmail.com",
            "","화성행궁 12번지", "test/s3/seller", CertificationStatus.APPROVED , store)
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_ADDRESS.getMessage());
    }


    @Test
    @DisplayName("판매자 정보 생성시 비어 있는 상세 주소로 인해 실한다")
    void fail_create_seller_with_invalid_detail_address() {
        assertThatThrownBy(() -> Seller.create("01012346789", "01012346789", "test1234@gmail.com",
            "경기도 수원시 팔달구","", "test/s3/seller", CertificationStatus.APPROVED ,store)
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_DETAIL_ADDRESS.getMessage());
    }

    @Test
    @DisplayName("판매자 정보 생성시 비어 있는 프로필 이미지 주소로 인해 실패한다")
    void fail_create_seller_with_invalid_profile_image_path() {
        assertThatThrownBy(() -> Seller.create("01012346789", "01012346789", "test1234@gmail.com",
            "경기도 수원시 팔달구","화성행궁 12번지", "", CertificationStatus.APPROVED ,store )
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_PROFILE.getMessage());
    }



    @Test
    @DisplayName("판매자 정보 생성시 비어 있는 상태값으로 인해 실패한다")
    void fail_create_seller_with_invalid_status_address() {
        // act & assert
        assertThatThrownBy(() -> Seller.create("01012346789", "01012346789", "test1234@gmail.com",
            "경기도 수원시 팔달구","화성행궁 12번지", "test/s3/seller", null , store)
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_CERTIFICATION_STATUS.getMessage());
    }


    @Test
    @DisplayName("판매자 정보 생성시 Store가 null이면 실패한다")
    void fail_create_seller_with_null_store() {
        assertThatThrownBy(() -> Seller.create("01012346789", "01012346789", "test1234@gmail.com",
            "경기도 수원시 팔달구","화성행궁 12번지", "test/s3/seller", CertificationStatus.APPROVED, null)
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_STORE.getMessage());
    }

}
