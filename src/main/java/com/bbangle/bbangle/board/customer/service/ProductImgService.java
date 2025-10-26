package com.bbangle.bbangle.board.customer.service;

import com.bbangle.bbangle.board.customer.dto.ProductImgResponse;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.board.repository.ProductImgRepository;
import com.bbangle.bbangle.image.service.S3Service;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductImgService {

    private final ProductImgRepository productImgRepository;
    private final S3Service s3Service;

    private static final String PRODUCT_IMAGE_FOLDER = "product-images";

    /**
     * 단일 이미지 업로드 (Board 연결 없이)
     */
    @Transactional
    public ProductImgResponse uploadSingle(MultipartFile imageFile) {
        String imageUrl = s3Service.saveAndReturnWithCdn(PRODUCT_IMAGE_FOLDER, imageFile);

        ProductImg productImg = ProductImg.builder()
            .url(imageUrl)
            .board(null)
            .build();

        ProductImg savedProductImg = productImgRepository.save(productImg);

        return new ProductImgResponse(savedProductImg.getId(),
            savedProductImg.getUrl());
    }

    /**
     * 다중 이미지 업로드 (Board 연결 없이)
     */
    @Transactional
    public List<ProductImgResponse> uploadMultiple(List<MultipartFile> imageFiles) {
        List<ProductImgResponse> responses = new ArrayList<>();

        for (MultipartFile imageFile : imageFiles) {
            ProductImgResponse response = uploadSingle(imageFile);
            responses.add(response);
        }

        return responses;
    }

    /**
     * 이미지를 Board와 연결
     */
    @Transactional
    public void connectImagesToBoard(List<Long> imageIds, Board board) {
        List<ProductImg> productImgs = productImgRepository.findAllByIdInOrderByIdAsc(imageIds);

        int imgOrder = 0;
        for (ProductImg productImg : productImgs) {
            productImg.updateBoard(board, imgOrder);
            imgOrder++;
        }
    }
}
