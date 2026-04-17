package com.bbangle.bbangle.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("[단위 테스트] BbangleErrorCode")
class BbangleErrorCodeTest {

    @Nested
    @DisplayName("NOT_FOUND_REQUEST 에러 코드 테스트")
    class NotFoundRequestTest {

        @Test
        @DisplayName("NOT_FOUND_REQUEST의 코드 값이 -989이다.")
        void notFoundRequest_has_correct_code() {
            assertThat(BbangleErrorCode.NOT_FOUND_REQUEST.getCode()).isEqualTo(-989);
        }

        @Test
        @DisplayName("NOT_FOUND_REQUEST의 메시지가 올바르다.")
        void notFoundRequest_has_correct_message() {
            assertThat(BbangleErrorCode.NOT_FOUND_REQUEST.getMessage()).isEqualTo("해당 요청을 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("NOT_FOUND_REQUEST의 HTTP 상태가 404 NOT_FOUND이다.")
        void notFoundRequest_has_correct_http_status() {
            assertThat(BbangleErrorCode.NOT_FOUND_REQUEST.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("코드로 NOT_FOUND_REQUEST를 조회할 수 있다.")
        void notFoundRequest_lookup_by_code() {
            BbangleErrorCode found = BbangleErrorCode.of(-989);
            assertThat(found).isEqualTo(BbangleErrorCode.NOT_FOUND_REQUEST);
        }

        @Test
        @DisplayName("메시지로 NOT_FOUND_REQUEST를 조회할 수 있다.")
        void notFoundRequest_lookup_by_message() {
            BbangleErrorCode found = BbangleErrorCode.of("해당 요청을 찾을 수 없습니다.");
            assertThat(found).isEqualTo(BbangleErrorCode.NOT_FOUND_REQUEST);
        }
    }

    @Nested
    @DisplayName("BbangleErrorCode.of() 조회 테스트")
    class OfMethodTest {

        @Test
        @DisplayName("존재하지 않는 코드로 조회하면 BbangleException이 발생한다.")
        void of_with_unknown_code_throws_exception() {
            assertThatThrownBy(() -> BbangleErrorCode.of(Integer.MIN_VALUE))
                .isInstanceOf(BbangleException.class);
        }

        @Test
        @DisplayName("존재하지 않는 메시지로 조회하면 BbangleException이 발생한다.")
        void of_with_unknown_message_throws_exception() {
            assertThatThrownBy(() -> BbangleErrorCode.of("존재하지않는메시지"))
                .isInstanceOf(BbangleException.class);
        }
    }
}