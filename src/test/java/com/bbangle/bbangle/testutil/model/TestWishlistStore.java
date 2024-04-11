package com.bbangle.bbangle.testutil.model;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.wishListStore.domain.WishlistStore;

public class TestWishlistStore extends TestModel<WishlistStore>{
    private Store store;
    private Member member;

    public TestWishlistStore(Store store, Member member){
        this.store = store;
        this.member = member;
    }

    @Override
    public WishlistStore getModel(){
        return WishlistStore.builder()
                .store(store)
                .member(member)
                .build();
    }
}
