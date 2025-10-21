package com.bbangle.bbangle.board.controller.mapper;

import com.bbangle.bbangle.store.dto.StoreResponse;
import com.bbangle.bbangle.board.service.dto.StoreInfo;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface StoreMapper {

    StoreResponse.StoreDetail toStoreDetailResponse(StoreInfo.StoreDetail storeDetail);

}
