package com.bbangle.bbangle.repository.impl;

import static com.bbangle.bbangle.model.QWishlistFolder.wishlistFolder;

import com.bbangle.bbangle.dto.FolderResponseDto;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.model.QBoard;
import com.bbangle.bbangle.model.QWishlistFolder;
import com.bbangle.bbangle.model.QWishlistProduct;
import com.bbangle.bbangle.model.WishlistFolder;
import com.bbangle.bbangle.repository.queryDsl.WishListFolderQueryDSLRepository;
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

    private final JPAQueryFactory queryFactory;

    @Override
    public List<FolderResponseDto> findMemberFolderList(Member member) {
        QWishlistFolder folder = wishlistFolder;
        QWishlistProduct wishedBoard = QWishlistProduct.wishlistProduct;
        QBoard board = QBoard.board;

        List<Tuple> fetch = queryFactory.select(
                folder.id,
                folder.folderName,
                board.profile)
            .from(folder)
            .leftJoin(wishedBoard)
            .on(wishedBoard.wishlistFolder.eq(folder))
            .leftJoin(board)
            .on(wishedBoard.board.eq(board))
            .where(folder.member.eq(member)
                .and(folder.isDeleted.eq(false))
                .and(wishedBoard.isDeleted.eq(false)
                    .or(wishedBoard.isNull())))
            .fetch();

        Map<Long, List<Tuple>> groupedByFolderId = fetch.stream()
            .collect(Collectors.groupingBy(tuple -> tuple.get(folder.id)));

        List<FolderResponseDto> response = groupedByFolderId.entrySet()
            .stream()
            .map(entry -> {
                Long folderId = entry.getKey();
                List<Tuple> tuples = entry.getValue();

                String title = tuples.get(0)
                    .get(folder.folderName);

                List<String> productImages = tuples.stream()
                    .map(tuple -> tuple.get(board.profile))
                    .filter(Objects::nonNull)
                    .limit(4)
                    .collect(Collectors.toList());

                int count = productImages.isEmpty() ? 0 : tuples.size();

                return new FolderResponseDto(folderId, title, count, productImages);
            })
            .sorted(Comparator.comparing(FolderResponseDto::folderId)
                .reversed())
            .collect(Collectors.toList());

        FolderResponseDto defaultFolder = response.stream()
            .filter(folderResponse -> "기본 폴더".equals(folderResponse.title()))
            .findFirst()
            .orElse(null);

        if (defaultFolder != null) {
            response.remove(defaultFolder);
            response.add(0, defaultFolder);
        }

        return response;
    }

    @Override
    public List<WishlistFolder> findByMemberId(Long memberId) {
        QWishlistFolder wishlistFolder = QWishlistFolder.wishlistFolder;
        return queryFactory.selectFrom(wishlistFolder)
                .where(wishlistFolder.member.id.eq(memberId).and(wishlistFolder.isDeleted.eq(false)))
                .fetch();
    };

}
