package com.bbangle.bbangle.review.dao;

import com.querydsl.core.annotations.QueryProjection;

public record ReviewStatisticDao (
    Long boardId,
    Double averageRate,
    Long reviewCount
){

    @QueryProjection
    public ReviewStatisticDao{

    }

}
