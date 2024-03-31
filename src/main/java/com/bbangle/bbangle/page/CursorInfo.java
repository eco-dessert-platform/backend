package com.bbangle.bbangle.page;

public record CursorInfo (
    Boolean hasNext,
    Long rank,
    Long endSize
){

}
