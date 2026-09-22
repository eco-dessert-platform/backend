package com.bbangle.bbangle.board.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] ProductImg 도메인")
class ProductImgTest {

    @Nested
    @DisplayName("copyOf 메서드")
    class CopyOfTest {

        @Test
        @DisplayName("url과 imgOrder를 그대로 복제한 새 인스턴스를 반환한다")
        void success() {

            // given
            ProductImg original = ProductImg.builder()
                .url("https://cdn.example.com/thumbnail.png")
                .imgOrder(0)
                .build();

            // when
            ProductImg copied = ProductImg.copyOf(original);

            // then
            assertThat(copied).isNotSameAs(original);
            assertThat(copied.getUrl()).isEqualTo(original.getUrl());
            assertThat(copied.getImgOrder()).isEqualTo(original.getImgOrder());
            assertThat(copied.getBoard()).isNull();
        }
    }
}
