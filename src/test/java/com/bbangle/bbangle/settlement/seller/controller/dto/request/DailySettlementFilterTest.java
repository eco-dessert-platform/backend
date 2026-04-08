package com.bbangle.bbangle.settlement.seller.controller.dto.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.settlement.seller.service.model.SellerSettlementCommand.DailySettlementSearchCommand;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DisplayName("[DTO] DailySettlementFilter")
class DailySettlementFilterTest {

    private static final Long SELLER_ID = 1L;
    private static final Pageable PAGEABLE = PageRequest.of(0, 10);

    @Nested
    @DisplayName("toCommand() - 일반 페이지네이션 조회 변환")
    class ToCommandTest {

    @Test
    @DisplayName("정상적인 날짜 범위로 커맨드 변환이 성공한다")
    void 정상적인_날짜_범위로_커맨드_변환이_성공한다() {
        // given
        DailySettlementFilter filter = new DailySettlementFilter(
            LocalDate.of(2025, 3, 1),
            LocalDate.of(2025, 3, 7)
        );

        // when
        DailySettlementSearchCommand command = filter.toCommand(SELLER_ID, PAGEABLE);

        // then
        assertThat(command.sellerId()).isEqualTo(SELLER_ID);
        assertThat(command.startDate()).isEqualTo(LocalDate.of(2025, 3, 1));
        assertThat(command.endDate()).isEqualTo(LocalDate.of(2025, 3, 7));
    }

    @Test
    @DisplayName("시작일이 종료일보다 늦으면 예외가 발생한다")
    void 시작일이_종료일보다_늦으면_예외가_발생한다() {
        // given
        DailySettlementFilter filter = new DailySettlementFilter(
            LocalDate.of(2025, 3, 7),
            LocalDate.of(2025, 3, 1)
        );

        // when & then
        assertThatThrownBy(() -> filter.toCommand(SELLER_ID, PAGEABLE))
            .isInstanceOf(BbangleException.class);
    }

    @Test
    @DisplayName("날짜가 null이면 유효성 검증을 통과한다")
    void 날짜가_null이면_유효성_검증을_통과한다() {
        // given
        DailySettlementFilter filter = new DailySettlementFilter(null, null);

        // when
        DailySettlementSearchCommand command = filter.toCommand(SELLER_ID, PAGEABLE);

        // then
        assertThat(command.startDate()).isNull();
        assertThat(command.endDate()).isNull();
    }

    } // end ToCommandTest

    @Nested
    @DisplayName("validateForExcel() - 엑셀 다운로드 전용 검증")
    class ValidateForExcelTest {

        @Test
        @DisplayName("startDate와 endDate가 모두 있고 1개월 이내면 예외가 발생하지 않는다")
        void 정상_날짜_범위는_예외_없음() {
            DailySettlementFilter filter = new DailySettlementFilter(
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 3, 31)
            );

            assertThatCode(filter::validateForExcel).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("정확히 1개월 범위(startDate ~ startDate+1개월)는 허용된다")
        void 정확히_1개월은_허용된다() {
            // 2025-01-01 ~ 2025-02-01 = plusMonths(1)과 동일, isAfter = false → 통과
            DailySettlementFilter filter = new DailySettlementFilter(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 2, 1)
            );

            assertThatCode(filter::validateForExcel).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("1개월을 하루라도 초과하면 SETTLEMENT_DATE_RANGE_EXCEEDED 예외가 발생한다")
        void 한달_초과시_예외_발생() {
            // 2025-01-01 ~ 2025-02-02: plusMonths(1) = 2025-02-01, endDate(02-02).isAfter(02-01) = true
            DailySettlementFilter filter = new DailySettlementFilter(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 2, 2)
            );

            assertThatThrownBy(filter::validateForExcel)
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.SETTLEMENT_DATE_RANGE_EXCEEDED));
        }

        @Test
        @DisplayName("startDate가 null이면 SETTLEMENT_DATE_REQUIRED 예외가 발생한다")
        void startDate_null이면_예외_발생() {
            DailySettlementFilter filter = new DailySettlementFilter(null, LocalDate.of(2025, 3, 31));

            assertThatThrownBy(filter::validateForExcel)
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.SETTLEMENT_DATE_REQUIRED));
        }

        @Test
        @DisplayName("endDate가 null이면 SETTLEMENT_DATE_REQUIRED 예외가 발생한다")
        void endDate_null이면_예외_발생() {
            DailySettlementFilter filter = new DailySettlementFilter(LocalDate.of(2025, 3, 1), null);

            assertThatThrownBy(filter::validateForExcel)
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.SETTLEMENT_DATE_REQUIRED));
        }

        @Test
        @DisplayName("startDate가 endDate보다 이후이면 INVALID_SETTLEMENT_DATE_RANGE 예외가 발생한다")
        void startDate가_endDate보다_이후이면_예외_발생() {
            DailySettlementFilter filter = new DailySettlementFilter(
                LocalDate.of(2025, 3, 31),
                LocalDate.of(2025, 3, 1)
            );

            assertThatThrownBy(filter::validateForExcel)
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.INVALID_SETTLEMENT_DATE_RANGE));
        }

    } // end ValidateForExcelTest

}
