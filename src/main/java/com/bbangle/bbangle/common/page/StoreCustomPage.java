package com.bbangle.bbangle.common.page;

import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo.StoreInfo;
import java.util.List;
import java.util.function.Function;

public class StoreCustomPage<T> extends CustomPage<T> {

    public StoreCustomPage(T content, Long requestCursor, Boolean hasNext) {
        super(content, requestCursor, hasNext);
    }

    public static StoreCustomPage<List<SellerStoreInfo.StoreInfo>> from(
        List<SellerStoreInfo.StoreInfo> responseList,
        Long pageSize
    ) {
        boolean hasNext = responseList.size() > pageSize;
        Long requestCursor =
            !responseList.isEmpty() ? responseList.get(responseList.size() - 1).id() : 0L;

        List<StoreInfo> limitedResponseList = hasNext
            ? responseList.stream().limit(pageSize).toList()
            : responseList;

        return new StoreCustomPage<>(limitedResponseList, requestCursor, hasNext);
    }

    // 리스트 타입을 위한 map
    public <R> StoreCustomPage<List<R>> map(Function<T, List<R>> mapper) {
        List<R> mappedContent = mapper.apply(this.getContent());
        return new StoreCustomPage<>(
            mappedContent,
            this.getNextCursor(),
            this.getHasNext()
        );
    }
}
