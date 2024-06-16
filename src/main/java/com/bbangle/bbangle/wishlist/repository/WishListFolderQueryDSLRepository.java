package com.bbangle.bbangle.wishlist.repository;

import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.dto.FolderResponseDto;
import com.bbangle.bbangle.member.domain.Member;

import java.util.List;

public interface WishListFolderQueryDSLRepository {

    List<FolderResponseDto> findMemberFolderList(Member member);
    List<WishListFolder> findByMemberId(Long memberId);

}

