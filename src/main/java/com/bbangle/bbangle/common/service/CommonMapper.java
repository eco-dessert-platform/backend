package com.bbangle.bbangle.common.service;

import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import com.bbangle.bbangle.wishlist.domain.WishListStore;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Objects;

import static com.bbangle.bbangle.wishlist.domain.QWishListBoard.wishListBoard;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface CommonMapper {

    @Named("isWishedStore")
    default Boolean isWishedStore(WishListStore wishListStore) {
        return !Objects.isNull(wishListStore) && !Objects.isNull(wishListStore.getId());
    }

    @Named("isWishedBoard")
    default Boolean isWishedBoard(WishListBoard wishListBoard) {
        return !Objects.isNull(wishListBoard) && !Objects.isNull(wishListBoard.getId());
    }

}
