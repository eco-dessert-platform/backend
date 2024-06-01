package com.bbangle.bbangle.admin.service;

import static com.bbangle.bbangle.image.domain.ImageCategory.BOARD;
import static com.bbangle.bbangle.image.domain.ImageCategory.BOARD_DETAIL;
import static com.bbangle.bbangle.image.domain.ImageCategory.STORE;

import com.bbangle.bbangle.admin.dto.AdminBoardRequestDto;
import com.bbangle.bbangle.admin.dto.AdminProductRequestDto;
import com.bbangle.bbangle.admin.dto.AdminStoreRequestDto;
import com.bbangle.bbangle.admin.repository.AdminBoardImgRepository;
import com.bbangle.bbangle.admin.repository.AdminBoardRepository;
import com.bbangle.bbangle.admin.repository.AdminProductRepository;
import com.bbangle.bbangle.admin.repository.AdminStoreRepository;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.image.service.ImageService;
import com.bbangle.bbangle.store.domain.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminStoreRepository adminStoreRepository;
    private final AdminBoardRepository adminBoardRepository;
    private final AdminBoardImgRepository adminBoardImgRepository;
    private final AdminProductRepository adminProductRepository;
    private final ImageService imageService;

    @Override
    @Transactional
    public Long uploadStore(AdminStoreRequestDto adminStoreRequestDto, MultipartFile profile) {

        var store = adminStoreRepository.save(Store.builder()
            .identifier(adminStoreRequestDto.identifier())
            .name(adminStoreRequestDto.title())
            .introduce(adminStoreRequestDto.introduce())
            .build());

        Long storeId = store.getId();
        String imagePath = imageService.save(STORE, profile, storeId);
        adminStoreRepository.save(store.updateProfile(imagePath)); // FIXME: 업데이트 해주지 말고 따로 가져오도록 변경 필요

        return storeId;
    }

    @Override
    public Long uploadBoard(
        MultipartFile profile,
        Long storeId,
        AdminBoardRequestDto adminBoardRequestDto
    ) {

        var board = adminBoardRepository.save(Board.builder()
            .store(Store.builder().id(storeId).build())
            .title(adminBoardRequestDto.title())
            .price(adminBoardRequestDto.price())
            .status(adminBoardRequestDto.status())
            .purchaseUrl(adminBoardRequestDto.purchaseUrl())
            .monday(adminBoardRequestDto.mon())
            .tuesday(adminBoardRequestDto.tue())
            .wednesday(adminBoardRequestDto.wed())
            .thursday(adminBoardRequestDto.thr())
            .friday(adminBoardRequestDto.fri())
            .saturday(adminBoardRequestDto.sat())
            .sunday(adminBoardRequestDto.sun())
            .status(true)
            .build());

        Long boardId = board.getId();
        String imagePath = imageService.save(BOARD, profile, boardId);
        adminBoardRepository.save(board.updateProfile(imagePath));

        return boardId;
    }

    @Override
    public Boolean uploadBoardImage(Long storeId, Long boardId, MultipartFile profile) {
        try {
            String imagePath = imageService.save(BOARD_DETAIL, profile, boardId, 0);
            adminBoardImgRepository.save(
                ProductImg.builder()
                    .board(Board.builder().id(boardId).build())
                    .url(imagePath)
                    .build()
            );
            return true;
        } catch (Exception ignore) {
            return false;
        }

    }

    @Override
    public void uploadProduct(
        Long storeId,
        Long boardId,
        AdminProductRequestDto adminProductRequestDto
    ) {
        adminProductRepository.save(
            Product.builder()
                .board(Board.builder().id(boardId).build())
                .title(adminProductRequestDto.title())
                .price(adminProductRequestDto.price())
                .category(Category.valueOf(adminProductRequestDto.category()))
                .glutenFreeTag(adminProductRequestDto.glutenFree())
                .sugarFreeTag(adminProductRequestDto.sugarFree())
                .highProteinTag(adminProductRequestDto.highProtein())
                .veganTag(adminProductRequestDto.vegan())
                .ketogenicTag(adminProductRequestDto.ketogenic())
                .build()
        );
    }

}
