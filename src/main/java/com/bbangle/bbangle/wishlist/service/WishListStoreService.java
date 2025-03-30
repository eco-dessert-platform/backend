package com.bbangle.bbangle.wishlist.service;

import static com.bbangle.bbangle.exception.BbangleErrorCode.NOTFOUND_WISH_INFO;
import static com.bbangle.bbangle.exception.BbangleErrorCode.STORE_NOT_FOUND;

import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.wishlist.domain.WishListStore;
import com.bbangle.bbangle.wishlist.dto.WishListStoreCustomPage;
import com.bbangle.bbangle.wishlist.dto.WishListStoreResponseDto;
import com.bbangle.bbangle.wishlist.repository.WishListStoreRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishListStoreService {

    public static final Long NON_MEMBER = 1L;

    private final WishListStoreRepository wishListStoreRepository;
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;

    public WishListStoreCustomPage<List<WishListStoreResponseDto>> getWishListStoresResponse(
        Long memberId, Long cursorId) {
        return wishListStoreRepository.getWishListStoreResponse(memberId, cursorId);

    }

    public boolean existWishListStore(Long memberId, Long storeId) {

        if (Objects.isNull(memberId) || memberId.equals(NON_MEMBER)) {
            return false;
        }

        return wishListStoreRepository.existsByStoreIdAndMemberId(memberId, storeId);
    }

    @Transactional
    public void save(Long memberId, Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new BbangleException(STORE_NOT_FOUND));
        Member member = memberRepository.findMemberById(memberId);
        wishListStoreRepository.findWishListStore(memberId, storeId)
            .ifPresentOrElse(WishListStore::changeDeletedFalse,
                () -> wishListStoreRepository.save(WishListStore.builder()
                    .member(member)
                    .store(store)
                    .build()));
    }

    @Transactional
    public void deleteStore(Long memberId, Long storeId) {
        WishListStore wishListStore = wishListStoreRepository.findWishListStore(memberId,
                storeId)
            .orElseThrow(() -> new BbangleException(NOTFOUND_WISH_INFO));
        wishListStore.delete();
    }

    @Transactional
    public void deletedByDeletedMember(Long memberId) {
        List<WishListStore> wishListStores = wishListStoreRepository.findWishListStores(
            memberId);
        if (wishListStores.size() != 0) {
            for (WishListStore wishListStore : wishListStores) {
                wishListStore.delete();
            }
        }
    }
}
