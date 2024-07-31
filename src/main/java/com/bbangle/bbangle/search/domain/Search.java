package com.bbangle.bbangle.search.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.member.domain.Member;
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

@Table(name = "search")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Search extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @Column(name = "is_deleted", columnDefinition = "tinyint")
    private boolean isDeleted;

    @Column(name = "keyword")
    private String keyword;

    @Builder
    public Search(Long memberId, String keyword) {
        this.member = Member.builder()
            .id(memberId)
            .build();
        this.keyword = keyword;
    }

}
