package com.bbangle.bbangle.store.admin.service;

import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.repository.StoreApplicationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminStoreApplicationService {

    private final StoreApplicationRepository storeApplicationRepository;

    @Transactional(readOnly = true)
    public List<StoreApplication> findAllByIds(List<Long> ids) {
        return storeApplicationRepository.findByIdIn(ids);
    }
}
