package com.bbangle.bbangle.wishlist.service;

import static com.bbangle.bbangle.exception.BbangleErrorCode.NOTFOUND_MEMBER;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.dto.FolderRequestDto;
import com.bbangle.bbangle.wishlist.dto.FolderResponseDto;
import com.bbangle.bbangle.wishlist.dto.FolderUpdateDto;
import com.bbangle.bbangle.wishlist.repository.WishListFolderRepository;
import com.bbangle.bbangle.wishlist.repository.impl.WishListFolderRepositoryImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishListFolderService {

    private static final String DEFAULT_FOLDER_NAME = "기본 폴더";

    private final MemberRepository memberRepository;
    private final WishListFolderRepository wishListFolderRepository;
    private final WishListFolderRepositoryImpl wishListFolderRepositoryImpl;

    @Transactional
    public Long create(Long memberId, FolderRequestDto requestDto) {
        Member member = memberRepository.findMemberById(memberId);
        validateMakingFolder(requestDto, member);

        WishListFolder folder = WishListFolder.builder()
            .member(member)
            .folderName(requestDto.title())
            .isDeleted(false)
            .build();

        return wishListFolderRepository.save(folder)
            .getId();
    }

    @Transactional(readOnly = true)
    public List<FolderResponseDto> getList(Long memberId) {
        Member member = memberRepository.findMemberById(memberId);

        return wishListFolderRepository.findMemberFolderList(member);
    }

    @Transactional
    public void delete(Long folderId, Long memberId) {
        Member member = memberRepository.findMemberById(memberId);

        WishListFolder wishlistFolder = wishListFolderRepository.findByMemberAndId(member, folderId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.FOLDER_NOT_FOUND));

        validateIsDeleteAvailable(wishlistFolder);

        wishlistFolder.delete();
    }

    @Transactional
    public void update(Long memberId, Long folderId, FolderUpdateDto updateDto) {
        Member member = memberRepository.findMemberById(memberId);

        WishListFolder wishlistFolder = wishListFolderRepository.findByMemberAndId(member, folderId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.FOLDER_NOT_FOUND));

        validateWishListFolder(wishlistFolder);

        wishlistFolder.updateTitle(updateDto.title());
    }

    @Transactional
    public void deletedByDeletedMember(Long memberId) {
        List<WishListFolder> wishListFolders = wishListFolderRepositoryImpl.findByMemberId(
            memberId);
        for (WishListFolder wishListFolder : wishListFolders) {
            wishListFolder.delete();
        }
    }

    private void validateWishListFolder(WishListFolder wishlistFolder) {
        if(wishlistFolder.isDeleted()){
           throw new BbangleException(BbangleErrorCode.CANNOT_UPDATE_ALREADY_DELETED_FOLDER);
        }

        if (wishlistFolder.getFolderName()
            .equals(DEFAULT_FOLDER_NAME)) {
            throw new BbangleException(BbangleErrorCode.DEFAULT_FOLDER_NAME_CANNOT_CHNAGE);
        }
    }

    private static void validateIsDeleteAvailable(WishListFolder wishlistFolder) {
        if (wishlistFolder.getFolderName()
            .equals(DEFAULT_FOLDER_NAME)) {
            throw new BbangleException(BbangleErrorCode.CANNOT_DELETE_DEFAULT_FOLDER);
        }

        if (wishlistFolder.isDeleted()) {
            throw new BbangleException(BbangleErrorCode.FOLDER_ALREADY_DELETED);
        }
    }

    private void validateMakingFolder(FolderRequestDto requestDto, Member member) {
        int folderCount = wishListFolderRepository.getFolderCount(member);
        if (wishListFolderRepository.existsByFolderNameAndMember(requestDto.title(), member)) {
            throw new BbangleException(BbangleErrorCode.FOLDER_NAME_ALREADY_EXIST);
        }
        if (folderCount >= 10) {
            throw new BbangleException(BbangleErrorCode.OVER_MAX_FOLDER);
        }
    }

}
