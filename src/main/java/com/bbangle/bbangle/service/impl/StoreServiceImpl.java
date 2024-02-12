package com.bbangle.bbangle.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import com.bbangle.bbangle.dto.StoreDetailResponseDto;
import com.bbangle.bbangle.dto.StoreResponseDto;
import com.bbangle.bbangle.model.Store;
import com.bbangle.bbangle.repository.StoreRepository;
import com.bbangle.bbangle.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;

    @Override
    public StoreDetailResponseDto getStoreDetailResponse(Long StoreId) {
        return storeRepository.getStoreDetailResponseDto(StoreId);
    }

    @Override
    public Slice<StoreResponseDto> getList(Pageable pageable) {
        Slice<Store> sliceBy = storeRepository.findSliceBy(pageable);

        List<StoreResponseDto> dtoList = sliceBy.getContent()
            .stream()
            .map(StoreResponseDto::fromWithoutLogin)
            .collect(Collectors.toList());

        return new SliceImpl<>(dtoList, pageable, sliceBy.hasNext());
    }


}
