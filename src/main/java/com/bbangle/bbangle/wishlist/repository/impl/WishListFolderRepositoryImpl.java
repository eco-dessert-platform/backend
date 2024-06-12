package com.bbangle.bbangle.wishlist.repository.impl;


import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.bbangle.bbangle.wishlist.domain.QWishListFolder;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.repository.WishListFolderQueryDSLRepository;
import com.bbangle.bbangle.wishlist.dto.FolderResponseDto;
import com.bbangle.bbangle.member.domain.Member;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WishListFolderRepositoryImpl implements WishListFolderQueryDSLRepository {

    private static final String DEFAULT_FOLDER_NAME = "기본 폴더";
    private static final QWishListFolder wishListFolder = QWishListFolder.wishListFolder;
    private static final QWishListBoard wishedBoard = QWishListBoard.wishListBoard;
    private static final QBoard board = QBoard.board;

    private final JPAQueryFactory queryFactory;


    @Override
    public List<FolderResponseDto> findMemberFolderList(Member member) {
        List<Tuple> fetch = queryFactory.select(
                wishListFolder.id,
                wishListFolder.folderName,
                board.profile)
            .from(wishListFolder)
            .leftJoin(wishedBoard)
            .on(wishedBoard.wishlistFolderId.eq(wishListFolder.id))
            .leftJoin(board)
            .on(wishedBoard.boardId.eq(board.id))
            .where(wishListFolder.member.eq(member)
                .and(wishListFolder.isDeleted.eq(false)))
            .fetch();

        Map<Long, List<Tuple>> groupedByFolderId = fetch.stream()
            .collect(Collectors.groupingBy(tuple -> tuple.get(wishListFolder.id)));

        List<FolderResponseDto> response = groupedByFolderId.entrySet()
            .stream()
            .map(entry -> {
                Long folderId = entry.getKey();
                List<Tuple> tuples = entry.getValue();

                String title = tuples.get(0)
                    .get(wishListFolder.folderName);

                List<String> productImages = tuples.stream()
                    .map(tuple -> tuple.get(board.profile))
                    .filter(Objects::nonNull)
                    .limit(4)
                    .toList();

                int count = productImages.isEmpty() ? 0 : (int) tuples.stream()
                    .map(tuple -> tuple.get(board.profile))
                    .filter(Objects::nonNull)
                    .count();

                return new FolderResponseDto(folderId, title, count, productImages);
            })
            .sorted(Comparator.comparing(FolderResponseDto::folderId)
                .reversed())
            .collect(Collectors.toList());

        FolderResponseDto defaultFolder = response.stream()
            .filter(folderResponse -> DEFAULT_FOLDER_NAME.equals(folderResponse.title()))
            .findFirst()
            .orElse(null);

        if (defaultFolder != null) {
            response.remove(defaultFolder);
            response.add(0, defaultFolder);
        }

        return response;
    }

    @Override
    public List<WishListFolder> findByMemberId(Long memberId) {

        return queryFactory.selectFrom(wishListFolder)
            .where(wishListFolder.member.id.eq(memberId)
                .and(wishListFolder.isDeleted.eq(false)))
            .fetch();
    }

}
