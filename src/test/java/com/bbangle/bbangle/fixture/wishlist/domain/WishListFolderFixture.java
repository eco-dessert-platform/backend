package com.bbangle.bbangle.fixture.wishlist.domain;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import org.springframework.test.util.ReflectionTestUtils;

public final class WishListFolderFixture {

    public static final String DEFAULT_FOLDER_NAME = "기본 폴더";

    private WishListFolderFixture() {
    }

    public static WishListFolder defaultFolder(Member member) {
        return WishListFolder.builder()
            .member(member)
            .folderName(DEFAULT_FOLDER_NAME)
            .build();
    }

    public static WishListFolder folderWithName(Member member, String folderName) {
        return WishListFolder.builder()
            .member(member)
            .folderName(folderName)
            .build();
    }

    public static WishListFolder withId(WishListFolder folder, Long id) {
        ReflectionTestUtils.setField(folder, "id", id);
        return folder;
    }
}
