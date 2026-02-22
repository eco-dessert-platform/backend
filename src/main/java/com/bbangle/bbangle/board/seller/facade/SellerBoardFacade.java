package com.bbangle.bbangle.board.seller.facade;

import com.bbangle.bbangle.board.seller.facade.command.CreateBoardFacadeCommand;
import com.bbangle.bbangle.board.seller.facade.command.UpdateBoardFacadeCommand;
import com.bbangle.bbangle.board.seller.service.SellerBoardService;
import com.bbangle.bbangle.board.seller.service.command.BoardDetailCommand;
import com.bbangle.bbangle.board.seller.service.command.ProductImgCommand;
import com.bbangle.bbangle.board.seller.service.info.BoardInfo;
import com.bbangle.bbangle.common.service.HtmlContentProcessor;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.seller.service.SellerStoreService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerBoardFacade {

    private static final String BOARD_IMAGE_FOLDER = "board-images";
    private static final String BOARD_DETAIL_IMAGE_FOLDER = "board-detail-images";

    private final SellerBoardService sellerBoardService;
    private final SellerStoreService sellerStoreService;
    private final S3Service s3Service;
    private final HtmlContentProcessor htmlContentProcessor;

    public BoardInfo createBoard(CreateBoardFacadeCommand command) {
        Store store = sellerStoreService.findStore(command.storeId());

        List<String> uploadedUrls = new ArrayList<>();
        try {
            List<ProductImgCommand> productImgCommands = uploadProductImages(
                command.thumbnailImgFile(),
                command.productImgs(),
                uploadedUrls
            );

            BoardDetailCommand boardDetailCommand = processBoardDetailContent(
                command.boardDetailRequest().getContent(),
                command.boardDetailImages(),
                uploadedUrls
            );

            return sellerBoardService.createBoard(
                command.toServiceCommand(store, productImgCommands, boardDetailCommand)
            );
        } catch (Exception e) {
            rollbackUploadedImages(uploadedUrls);
            throw e;
        }
    }

    private BoardDetailCommand processBoardDetailContent(
        String content,
        List<MultipartFile> detailImages,
        List<String> uploadedUrls
    ) {
        if (detailImages == null || detailImages.isEmpty()) {
            return new BoardDetailCommand(htmlContentProcessor.sanitize(content));
        }

        Map<String, String> cdnUrlMap = new HashMap<>();
        for (MultipartFile image : detailImages) {
            String uuid = image.getOriginalFilename();
            String cdnUrl = s3Service.saveAndReturnWithCdn(BOARD_DETAIL_IMAGE_FOLDER, image);
            cdnUrlMap.put(uuid, cdnUrl);
            uploadedUrls.add(cdnUrl);
        }

        String convertedContent = htmlContentProcessor.changeToCdn(content, cdnUrlMap);
        return new BoardDetailCommand(htmlContentProcessor.sanitize(convertedContent));
    }

    private List<ProductImgCommand> uploadProductImages(
        MultipartFile thumbnailImgFile,
        List<MultipartFile> subImgFiles,
        List<String> uploadedUrls
    ) {
        List<ProductImgCommand> productImgCommands = new ArrayList<>();

        String thumbnailUrl = s3Service.saveAndReturnWithCdn(BOARD_IMAGE_FOLDER, thumbnailImgFile);
        uploadedUrls.add(thumbnailUrl);
        productImgCommands.add(new ProductImgCommand(thumbnailUrl, 0));

        if (subImgFiles != null) {
            for (int i = 0; i < subImgFiles.size(); i++) {
                String subUrl = s3Service.saveAndReturnWithCdn(BOARD_IMAGE_FOLDER, subImgFiles.get(i));
                uploadedUrls.add(subUrl);
                productImgCommands.add(new ProductImgCommand(subUrl, i + 1));
            }
        }

        return productImgCommands;
    }

    public BoardInfo updateBoard(UpdateBoardFacadeCommand command) {
        List<String> uploadedUrls = new ArrayList<>();
        try {
            String thumbnailUrl;
            if (command.thumbnailImgFile() != null) {
                thumbnailUrl = s3Service.saveAndReturnWithCdn(BOARD_IMAGE_FOLDER, command.thumbnailImgFile());
                uploadedUrls.add(thumbnailUrl);
            } else {
                thumbnailUrl = command.existingThumbnailUrl();
            }
            if (thumbnailUrl == null) {
                throw new BbangleException(BbangleErrorCode.MISSING_BOARD_THUMBNAIL);
            }

            List<ProductImgCommand> productImgCommands = buildProductImgCommands(
                thumbnailUrl,
                command.newSubImages(),
                command.existingSubImageUrls(),
                uploadedUrls
            );

            BoardDetailCommand boardDetailCommand = processBoardDetailContent(
                command.boardDetailRequest().getContent(),
                command.boardDetailImages(),
                uploadedUrls
            );

            return sellerBoardService.updateBoard(
                command.toServiceCommand(productImgCommands, boardDetailCommand)
            );
        } catch (Exception e) {
            rollbackUploadedImages(uploadedUrls);
            throw e;
        }
    }

    private List<ProductImgCommand> buildProductImgCommands(
        String thumbnailUrl,
        List<MultipartFile> newSubImages,
        List<String> existingSubImageUrls,
        List<String> uploadedUrls
    ) {
        List<ProductImgCommand> commands = new ArrayList<>();
        commands.add(new ProductImgCommand(thumbnailUrl, 0));

        int order = 1;
        if (existingSubImageUrls != null) {
            for (String url : existingSubImageUrls) {
                commands.add(new ProductImgCommand(url, order++));
            }
        }
        if (newSubImages != null) {
            for (MultipartFile file : newSubImages) {
                String url = s3Service.saveAndReturnWithCdn(BOARD_IMAGE_FOLDER, file);
                uploadedUrls.add(url);
                commands.add(new ProductImgCommand(url, order++));
            }
        }
        return commands;
    }

    private void rollbackUploadedImages(List<String> uploadedUrls) {
        for (String url : uploadedUrls) {
            try {
                s3Service.deleteImage(url);
            } catch (Exception e) {
                log.error("Board 생성 실패로 인한 S3 이미지 롤백 중 오류 발생: {}", url, e);
            }
        }
    }
}
