package com.bbangle.bbangle.board.repository.folder.cursor;

import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.board.sort.SortType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BoardInFolderCursorGeneratorMapping {

    private final WishListRecentBoardInFolderCursorGenerator wishListRecentBoardInFolderCursorGenerator;
    private final LowPriceInFolderBoardInFolderCursorGenerator lowPriceInFolderBoardInFolderCursorGenerator;
    private final PopularBoardInFolderCursorGenerator popularBoardInFolderCursorGenerator;

    public BoardInFolderCursorGenerator mappingCursorGenerator(FolderBoardSortType sortType) {
        if(sortType == null){
            return wishListRecentBoardInFolderCursorGenerator;
        }

        if (sortType == FolderBoardSortType.LOW_PRICE) {
            return lowPriceInFolderBoardInFolderCursorGenerator;
        }

        if (sortType == FolderBoardSortType.POPULAR) {
            return popularBoardInFolderCursorGenerator;
        }

        return wishListRecentBoardInFolderCursorGenerator;
    }

}
