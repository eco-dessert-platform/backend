package com.bbangle.bbangle.wishListStore.service;

import com.bbangle.bbangle.wishListStore.dto.WishListStoreCursorPagingDto;
import com.bbangle.bbangle.wishListStore.dto.WishListStorePagingDto;
import com.bbangle.bbangle.exception.MemberNotFoundException;
import com.bbangle.bbangle.exception.NoSuchMemberidOrStoreIdException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.wishListStore.domain.WishlistStore;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.wishListStore.dto.WishListStoreResponseDto;
import com.bbangle.bbangle.wishListStore.repository.WishListStoreRepository;
import com.bbangle.bbangle.wishListStore.repository.WishListStoreRepositoryImpl;
import com.bbangle.bbangle.wishListStore.service.WishListStoreService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishListStoreServiceImpl implements WishListStoreService {

    private final WishListStoreRepository wishListStoreRepository;
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final WishListStoreRepositoryImpl wishListStoreRepositoryImpl;

    @Override
    @Transactional(readOnly = true)
    public WishListStorePagingDto getWishListStoresRes(Long memberId, Pageable pageable) {
        return WishListStorePagingDto.of(wishListStoreRepositoryImpl.getWishListStoreRes(memberId, pageable));

    }

    @Transactional
    public void save(Long memberId, Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 스토어 입니다"));
        Member member = memberRepository.findById(memberId)
            .orElseThrow(MemberNotFoundException::new);
        wishListStoreRepository.save(WishlistStore.builder()
            .member(member)
            .store(store)
            .build());
    }

    @Transactional
    public void deleteStore(Long memberId, Long storeId) {
        WishlistStore wishListStore = wishListStoreRepositoryImpl.findWishListStore(memberId,
                storeId)
            .orElseThrow(() -> new NoSuchMemberidOrStoreIdException());
        wishListStore.delete();
    }

    @Transactional
    @Override
    public void deletedByDeletedMember(Long memberId) {
        List<WishlistStore> wishListStores = wishListStoreRepositoryImpl.findWishListStores(memberId);
        if (wishListStores.size() !=0){
            for (WishlistStore wishListStore : wishListStores) {
                wishListStore.delete();
            }
        }
    }

    @Override
    public WishListStoreCursorPagingDto getWishListStoresResByCursor(Long memberId, Long cursorId, int size) {
        List<WishListStoreResponseDto> wishListStoreResponseDtos =
        wishListStoreRepositoryImpl.getWishListStoreResByCursor(memberId, cursorId, size);
        return WishListStoreCursorPagingDto.of(wishListStoreResponseDtos, size);
    }
}
