package com.bbangle.bbangle.wishlist.repository;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.wishlist.customer.dto.FolderResponseDto;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import java.util.List;

public interface WishListFolderQueryDSLRepository {

    List<FolderResponseDto> findMemberFolderList(Member member);

    List<WishListFolder> findByMemberId(Long memberId);

}

