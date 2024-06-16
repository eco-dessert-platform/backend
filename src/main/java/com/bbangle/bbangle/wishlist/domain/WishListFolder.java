package com.bbangle.bbangle.wishlist.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.wishlist.validator.WishListFolderValidator;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "Wishlist_folder")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WishListFolder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @Column(name = "folder_name")
    private String folderName;

    @Column(name = "is_deleted", columnDefinition = "tinyint")
    private boolean isDeleted;

    @Builder
    private WishListFolder(
        Long id,
        Member member,
        String folderName,
        boolean isDeleted
    ){
        WishListFolderValidator.validateMember(member);
        WishListFolderValidator.validateTitle(folderName);

        this.id = id;
        this.member = member;
        this.folderName = folderName;
        this.isDeleted = isDeleted;
    }

    public void updateTitle(String title) {
        WishListFolderValidator.validateTitle(folderName);
        this.folderName = title;
    }

    public void delete() {
        this.isDeleted = true;
    }

}
