package com.bbangle.bbangle.page;

import com.bbangle.bbangle.store.dto.StoreResponseDto;
import java.util.List;
import lombok.Getter;

@Getter
public class StoreCustomPage<T> extends CustomPage<T> {

    public StoreCustomPage(T content, Long requestCursor, Boolean hasNext) {
        super(content, requestCursor, hasNext);
    }

    public static StoreCustomPage<List<StoreResponseDto>> from(
        List<StoreResponseDto> storeResponseDtoList,
        Long requestCursor,
        Boolean hasNext
    ) {
        return new StoreCustomPage<>(storeResponseDtoList, requestCursor, hasNext);
    }

}
