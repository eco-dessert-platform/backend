package com.bbangle.bbangle.store.seller.controller.mapper;

import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.SearchResponse;
import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface SellerStoreMapper {

    @Mapping(source = "id", target = "storeId")
    @Mapping(source = "name", target = "storeName")
    SearchResponse toSearchResponse(Store store);

    List<SearchResponse> toSearchResponseList(List<Store> stores);
}
