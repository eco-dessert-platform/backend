package com.bbangle.bbangle.search.customer.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.util.Objects;

public class KeywordDto {

    private String keyword;

    @QueryProjection
    public KeywordDto(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        KeywordDto that = (KeywordDto) obj;
        return Objects.equals(keyword, that.keyword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyword);
    }
}
