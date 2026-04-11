package com.bbangle.bbangle.settlement.seller.controller.dto.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.settlement.seller.service.model.SellerSettlementCommand.SettlementItemSearchCommand;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DisplayName("[DTO] SettlementItemFilter")
class SettlementItemFilterTest {

    private static final Long SELLER_ID = 1L;
    private static final Pageable PAGEABLE = PageRequest.of(0, 10);

    @Nested
    @DisplayName("toCommand() - 일반 페이지네이션 조회 변환")
    class ToCommandTest {

        @Test
        @DisplayName("정상적인 날짜 범위로 커맨드 변환이 성공한다")
        void 정상적인_날짜_범위로_커맨드_변환이_성공한다() {
            // given
            SettlementItemFilter filter = new SettlementItemFilter(
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 3, 31)
            );

            // when
            SettlementItemSearchCommand command = filter.toCommand(SELLER_ID, PAGEABLE);

            // then
            assertThat(command.sellerId()).isEqualTo(SELLER_ID);
            assertThat(command.startDate()).isEqualTo(LocalDate.of(2025, 3, 1));
            assertThat(command.endDate()).isEqualTo(LocalDate.of(2025, 3, 31));
            assertThat(command.pageable()).isEqualTo(PAGEABLE);
        }

        @Test
        @DisplayName("시작일이 종료일보다 늦으면 예외가 발생한다")
        void 시작일이_종료일보다_늦으면_예외가_발생한다() {
            // given
            SettlementItemFilter filter = new SettlementItemFilter(
                LocalDate.of(2025, 3, 31),
                LocalDate.of(2025, 3, 1)
            );

            // when & then
            assertThatThrownBy(() -> filter.toCommand(SELLER_ID, PAGEABLE))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.INVALID_SETTLEMENT_DATE_RANGE));
        }

        @Test
        @DisplayName("날짜가 null이면 유효성 검증을 통과하고 커맨드에 null이 담긴다")
        void 날짜가_null이면_유효성_검증을_통과한다() {
            // given
            SettlementItemFilter filter = new SettlementItemFilter(null, null);

            // when
            SettlementItemSearchCommand command = filter.toCommand(SELLER_ID, PAGEABLE);

            // then
            assertThat(command.startDate()).isNull();
            assertThat(command.endDate()).isNull();
        }

        @Test
        @DisplayName("시작일만 null이면 유효성 검증을 통과한다")
        void 시작일만_null이면_유효성_검증을_통과한다() {
            // given
            SettlementItemFilter filter = new SettlementItemFilter(null, LocalDate.of(2025, 3, 31));

            // when & then
            assertThatCode(() -> filter.toCommand(SELLER_ID, PAGEABLE))
                .doesNotThrowAnyException();
        }

    } // end ToCommandTest

    @Nested
    @DisplayName("validateForExcel() - 엑셀 다운로드 전용 검증")
    class ValidateForExcelTest {

        @Test
        @DisplayName("정상적인 날짜 범위(1개월 이내)면 예외가 발생하지 않는다")
        void 정상_날짜_범위는_예외_없음() {
            // given
            SettlementItemFilter filter = new SettlementItemFilter(
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 3, 31)
            );

            // when & then
            assertThatCode(filter::validateForExcel).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("정확히 1개월 범위(startDate ~ startDate+1개월)는 허용된다")
        void 정확히_1개월은_허용된다() {
            // 2025-01-01 ~ 2025-02-01: plusMonths(1)과 동일, isAfter = false → 통과
            SettlementItemFilter filter = new SettlementItemFilter(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 2, 1)
            );

            // when & then
            assertThatCode(filter::validateForExcel).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("1개월을 하루라도 초과하면 SETTLEMENT_DATE_RANGE_EXCEEDED 예외가 발생한다")
        void 한달_초과시_예외_발생() {
            // 2025-01-01 ~ 2025-02-02: plusMonths(1) = 2025-02-01, endDate(02-02).isAfter(02-01) = true
            SettlementItemFilter filter = new SettlementItemFilter(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 2, 2)
            );

            // when & then
            assertThatThrownBy(filter::validateForExcel)
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.SETTLEMENT_DATE_RANGE_EXCEEDED));
        }

        @Test
        @DisplayName("startDate가 null이면 SETTLEMENT_DATE_REQUIRED 예외가 발생한다")
        void startDate_null이면_예외_발생() {
            // given
            SettlementItemFilter filter = new SettlementItemFilter(null, LocalDate.of(2025, 3, 31));

            // when & then
            assertThatThrownBy(filter::validateForExcel)
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.SETTLEMENT_DATE_REQUIRED));
        }

        @Test
        @DisplayName("endDate가 null이면 SETTLEMENT_DATE_REQUIRED 예외가 발생한다")
        void endDate_null이면_예외_발생() {
            // given
            SettlementItemFilter filter = new SettlementItemFilter(LocalDate.of(2025, 3, 1), null);

            // when & then
            assertThatThrownBy(filter::validateForExcel)
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.SETTLEMENT_DATE_REQUIRED));
        }

        @Test
        @DisplayName("startDate가 endDate보다 이후이면 INVALID_SETTLEMENT_DATE_RANGE 예외가 발생한다")
        void startDate가_endDate보다_이후이면_예외_발생() {
            // given
            SettlementItemFilter filter = new SettlementItemFilter(
                LocalDate.of(2025, 3, 31),
                LocalDate.of(2025, 3, 1)
            );

            // when & then
            assertThatThrownBy(filter::validateForExcel)
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.INVALID_SETTLEMENT_DATE_RANGE));
        }

    } // end ValidateForExcelTest

    @Nested
    @DisplayName("toExcelCommand() - 엑셀 커맨드 변환")
    class ToExcelCommandTest {

        @Test
        @DisplayName("sellerId와 날짜 범위가 엑셀 커맨드에 올바르게 담긴다")
        void 엑셀_커맨드로_올바르게_변환된다() {
            // given
            SettlementItemFilter filter = new SettlementItemFilter(
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 3, 31)
            );

            // when
            var command = filter.toExcelCommand(SELLER_ID);

            // then
            assertThat(command.sellerId()).isEqualTo(SELLER_ID);
            assertThat(command.startDate()).isEqualTo(LocalDate.of(2025, 3, 1));
            assertThat(command.endDate()).isEqualTo(LocalDate.of(2025, 3, 31));
        }

    } // end ToExcelCommandTest

}
