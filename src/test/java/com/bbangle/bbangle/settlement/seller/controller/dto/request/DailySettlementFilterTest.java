package com.bbangle.bbangle.settlement.seller.controller.dto.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.settlement.seller.service.model.SellerSettlementCommand.DailySettlementSearchCommand;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DisplayName("[DTO] DailySettlementFilter")
class DailySettlementFilterTest {

    private static final Long SELLER_ID = 1L;
    private static final Pageable PAGEABLE = PageRequest.of(0, 10);

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

}
