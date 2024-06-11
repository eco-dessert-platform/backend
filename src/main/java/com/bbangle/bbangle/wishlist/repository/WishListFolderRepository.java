package com.bbangle.bbangle.wishlist.repository;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishListFolderRepository extends JpaRepository<WishListFolder, Long>,
    WishListFolderQueryDSLRepository {

    @Query(value = "select count(folder) from WishListFolder folder where folder.member = :member and folder.isDeleted = false ")
    int getFolderCount(@Param("member") Member member);

    boolean existsByFolderNameAndMember(String folderName, Member member);

    @Query(value = "select folder from WishListFolder folder where folder.id = :folderId and folder.member.id = :memberId")
    Optional<WishListFolder> findByMemberIdAndId(Long memberId, Long folderId);

    Optional<WishListFolder> findByMemberAndId(Member member, Long folderId);

    Optional<WishListFolder> findByMemberAndFolderName(Member member, String FolderName);

}
