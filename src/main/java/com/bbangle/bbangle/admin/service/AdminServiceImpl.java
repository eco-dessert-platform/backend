package com.bbangle.bbangle.admin.service;

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
import com.bbangle.bbangle.common.image.repository.ObjectStorageRepository;
import com.bbangle.bbangle.store.domain.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final ObjectStorageRepository objectStorageRepository;


    String CDN_URL = "https://bbangree-oven.cdn.ntruss.com";
    String DEFAULT_PROFILE_FILE_NAME = "profile.jpg";
    String DEFAULT_SUBIMAGE_FOLDER_NAME = "subimage";
    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    @Override
    @Transactional
    public Long uploadStore(AdminStoreRequestDto adminStoreRequestDto, MultipartFile profile) {

        var store = adminStoreRepository.save(Store.builder()
                .identifier(adminStoreRequestDto.identifier())
                .name(adminStoreRequestDto.title())
                .introduce(adminStoreRequestDto.introduce())
                .build());

        Long storeId = store.getId();

        String profileUrl = String.format("%s/%s", storeId, DEFAULT_PROFILE_FILE_NAME);
        objectStorageRepository.createFile(BUCKET_NAME, profileUrl, profile);
        String profileCdnUrl = String.format("%s/%s", CDN_URL, profileUrl);
        adminStoreRepository.save(store.updateProfile(profileCdnUrl));

        return storeId;
    }

    @Override
    public Long uploadBoard(MultipartFile profile, Long storeId, AdminBoardRequestDto adminBoardRequestDto) {

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
        String profileUrl = String.format("%s/%s/%s", storeId, boardId, DEFAULT_PROFILE_FILE_NAME);
        objectStorageRepository.createFile(BUCKET_NAME, profileUrl, profile);
        String profileCdnUrl = String.format("%s/%s", CDN_URL, profileUrl);
        adminBoardRepository.save(board.updateProfile(profileCdnUrl));

        return boardId;
    }

    @Override
    public Boolean uploadBoardImage(Long storeId, Long boardId, MultipartFile profile) {
        int imgCount = adminBoardImgRepository.getBoardImageCount(boardId);
        String subimageName = String.format("%s.jpg",imgCount);
        String subimageUrl = String.format("%s/%s/%s/%s", storeId, boardId, DEFAULT_SUBIMAGE_FOLDER_NAME, subimageName);
        adminBoardImgRepository.save(
                ProductImg.builder()
                        .board(Board.builder().id(boardId).build())
                        .url(String.format("%s/%s/%s/%s/%s", CDN_URL, storeId, boardId, DEFAULT_SUBIMAGE_FOLDER_NAME, subimageName))
                        .build()
        );

        return objectStorageRepository.createFile(BUCKET_NAME, subimageUrl, profile);
    }

    @Override
    public void uploadProduct(Long storeId, Long boardId, AdminProductRequestDto adminProductRequestDto) {
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
