package com.bbangle.bbangle.repository;

import java.util.HashMap;
import java.util.List;
import com.bbangle.bbangle.dto.BoardDetailResponseDto;
import com.bbangle.bbangle.dto.BoardResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface BoardQueryDSLRepository {

    Slice<BoardResponseDto> getBoardResponseDto(String sort, Boolean glutenFreeTag, Boolean highProteinTag,
                                                Boolean sugarFreeTag, Boolean veganTag, Boolean ketogenicTag,
                                                String category, Integer minPrice, Integer maxPrice, Pageable pageable);

    Slice<BoardResponseDto> getAllByFolder(String sort, Pageable pageable, Long wishListFolderId, List<Long> boardIds);
    BoardDetailResponseDto getBoardDetailResponseDto(Long boardId);

    HashMap<Long, String> getAllBoardTitle();

}

