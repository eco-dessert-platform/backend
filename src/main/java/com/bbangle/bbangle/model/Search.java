package com.bbangle.bbangle.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

import lombok.*;


@Table(name = "search")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Search {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "is_deleted",  columnDefinition = "tinyint")
    private boolean isDeleted;

    @Column(name = "keyword")
    private String keyword;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public Search(Member member, String keyword, LocalDateTime createdAt){
        this.member = member;
        this.keyword = keyword;
        this.createdAt = createdAt;
    }
}
