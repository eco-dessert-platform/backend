package com.bbangle.bbangle.common.page;

import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo.StoreInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

public class StoreCustomPageTest {

    @Test
    @DisplayName("페이지 크기보다 많은 요소가 주어지면 hasNext가 true여야 한다")
    void testFrom_withMoreElementsThanPageSize() {
        // given
        long pageSize = 10;
        List<StoreInfo> list = LongStream.rangeClosed(1, pageSize + 1)
            .mapToObj(i -> StoreInfo.builder()
                .id(i)
                .name("Store " + i)
                .build())
            .toList();

        // when
        StoreCustomPage<List<StoreInfo>> result = StoreCustomPage.from(list, pageSize);

        // then
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getContent()).hasSize((int) pageSize);
        assertThat(result.getNextCursor()).isEqualTo(pageSize + 1);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
        assertThat(result.getContent().get((int) (pageSize - 1)).id()).isEqualTo(pageSize);
    }

    @Test
    @DisplayName("페이지 크기보다 적은 요소가 주어지면 hasNext가 false여야 한다")
    void testFrom_withLessElementsThanPageSize() {
        // given
        long pageSize = 10;
        List<StoreInfo> list = LongStream.rangeClosed(1, pageSize - 1)
            .mapToObj(i -> StoreInfo.builder()
                .id(i)
                .name("Store " + i)
                .build())
            .toList();

        // when
        StoreCustomPage<List<StoreInfo>> result = StoreCustomPage.from(list, pageSize);

        // then
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getContent()).hasSize((int) pageSize - 1);
        assertThat(result.getNextCursor()).isEqualTo(pageSize - 1);
    }

    @Test
    @DisplayName("페이지 크기와 동일한 수의 요소가 주어지면 hasNext가 false여야 한다")
    void testFrom_withEqualElementsAsPageSize() {
        // given
        long pageSize = 10;
        List<StoreInfo> list = LongStream.rangeClosed(1, pageSize)
            .mapToObj(i -> StoreInfo.builder()
                .id(i)
                .name("Store " + i)
                .build())
            .toList();

        // when
        StoreCustomPage<List<StoreInfo>> result = StoreCustomPage.from(list, pageSize);

        // then
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getContent()).hasSize((int) pageSize);
        assertThat(result.getNextCursor()).isEqualTo(pageSize);
    }

    @Test
    @DisplayName("빈 리스트가 주어지면 hasNext는 false이고 내용은 비어 있어야 한다")
    void testFrom_withEmptyList() {
        // given
        long pageSize = 10;
        List<StoreInfo> list = new ArrayList<>();

        // when
        StoreCustomPage<List<StoreInfo>> result = StoreCustomPage.from(list, pageSize);

        // then
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getNextCursor()).isEqualTo(0L);
    }
}
