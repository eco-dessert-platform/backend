package com.bbangle.bbangle.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SearchResponseDto {
    SearchBoardDto boards;
    SearchStoreDto stores;
}
