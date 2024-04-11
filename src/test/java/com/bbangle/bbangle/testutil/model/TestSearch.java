package com.bbangle.bbangle.testutil.model;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.search.domain.Search;

import java.time.LocalDateTime;

public class TestSearch extends TestModel<Search>{
    private Member member = null;

    private String keyword = "TEST KEYWORD";
    private LocalDateTime createdAt = LocalDateTime.now();

    public TestSearch setMember(Member member) {
        this.member = member;

        return this;
    }

    public TestSearch setKeyword(String keyword) {
        this.keyword = keyword;

        return this;
    }

    public TestSearch setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;

        return this;
    }

    @Override
    public Search getModel(){
        return Search.builder()
                .member(member)
                .keyword(keyword)
                .createdAt(createdAt)
                .build();
    }
}
