package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import net.datafaker.Faker;

public class WishlistFolderFixture {

    private static final Faker faker = new Faker();

    public static WishListFolder createWishlistFolder(Member member){
        String title = faker.book()
            .title();

        if(title.length() > 12){
            title = title.substring(0, 12);
        }

        return WishListFolder.builder()
            .folderName(title)
            .member(member)
            .isDeleted(false)
            .build();
    }

}
