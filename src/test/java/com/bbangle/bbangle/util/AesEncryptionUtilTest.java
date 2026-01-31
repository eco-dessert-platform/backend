package com.bbangle.bbangle.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] AesEncryptionUtil")
class AesEncryptionUtilTest {

    private AesEncryptionUtil aesEncryptionUtil;

    @BeforeEach
    void setUp() {
        String testSecretKey = "1234567890123456";
        aesEncryptionUtil = new AesEncryptionUtil(testSecretKey);
    }

    @Test
    @DisplayName("평문 암호화에 성공한다")
    void success_encrypt_plain_text() {
        // arrange
        String plainText = "123412341234";

        // act
        String encrypted = aesEncryptionUtil.encrypt(plainText);

        // assert
        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEqualTo(plainText);
    }

    @Test
    @DisplayName("암호문 복호화에 성공한다")
    void success_decrypt_encrypted_text() {
        // arrange
        String plainText = "123412341234";
        String encrypted = aesEncryptionUtil.encrypt(plainText);

        // act
        String decrypted = aesEncryptionUtil.decrypt(encrypted);

        // assert
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("null 값 암호화 시 null을 반환한다")
    void return_null_when_encrypt_null() {
        // act
        String result = aesEncryptionUtil.encrypt(null);

        // assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("빈 문자열 암호화 시 빈 문자열을 반환한다")
    void return_empty_when_encrypt_empty_string() {
        // act
        String result = aesEncryptionUtil.encrypt("");

        // assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("암호화/복호화 라운드트립 테스트")
    void success_round_trip_encryption() {
        // arrange
        String accountNumber = "1234567890123456";

        // act
        String encrypted = aesEncryptionUtil.encrypt(accountNumber);
        String decrypted = aesEncryptionUtil.decrypt(encrypted);

        // assert
        assertThat(decrypted).isEqualTo(accountNumber);
    }

    @Test
    @DisplayName("잘못된 키 길이로 인스턴스 생성 시 예외가 발생한다")
    void fail_create_with_invalid_key_length() {
        // arrange
        String invalidKey = "short";

        // act & assert
        assertThatThrownBy(() -> new AesEncryptionUtil(invalidKey))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("AES key must be 16, 24, or 32 bytes");
    }
}
