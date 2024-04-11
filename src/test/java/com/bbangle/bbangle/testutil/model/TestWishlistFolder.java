package com.bbangle.bbangle.testutil.model;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.wishListFolder.domain.WishlistFolder;

public class TestWishlistFolder extends TestModel<WishlistFolder>{
    private Long id = null;
    private Member member;
    private String folderName = "Default Folder";

    public TestWishlistFolder(Member member){
        this.member = member;
    }

    public TestWishlistFolder setId(Long id) {
        this.id = id;

        return this;
    }

    public TestWishlistFolder setFolderName(String folderName) {
        this.folderName = folderName;

        return this;
    }

    @Override
    public WishlistFolder getModel(){
        return WishlistFolder.builder()
                .id(id)
                .folderName(folderName)
                .member(member)
                .build();
    }
}
