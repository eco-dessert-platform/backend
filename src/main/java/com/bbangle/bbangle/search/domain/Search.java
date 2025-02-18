package com.bbangle.bbangle.search.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
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

    @Column(name = "keyword")
    private String keyword;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    public Search(Member member, String keyword) {
        this.member = member;
        this.keyword = keyword;
    }

    public static Search save(Long memberId, String keyword) {
        Member member = Member.builder().id(memberId).build();
        return new Search(member, keyword);
    }

}
