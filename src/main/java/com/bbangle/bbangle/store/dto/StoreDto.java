package com.bbangle.bbangle.store.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class StoreDto {

    private Long id;
    private String title;
    private String profile;
    private Boolean isWished;

    @QueryProjection
    public StoreDto(
        Long id,
        String title,
        String profile) {
        this.id = id;
        this.title = title;
        this.profile = profile;
    }

    public void updateWished(Boolean isWished) {
        this.isWished = isWished;
    }
}
