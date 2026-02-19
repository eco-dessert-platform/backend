package com.bbangle.bbangle.board.seller.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductImgRepository;
import com.bbangle.bbangle.board.seller.service.command.CreateBoardServiceCommand;
import com.bbangle.bbangle.board.seller.service.command.ProductImgCommand;
import com.bbangle.bbangle.board.seller.service.command.UpdateBoardServiceCommand;
import com.bbangle.bbangle.board.seller.service.command.UpdateProductCommand;
import com.bbangle.bbangle.board.seller.service.info.BoardInfo;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerBoardService {

    private final BoardRepository boardRepository;
    private final ProductImgRepository productImgRepository;
    private final SellerRepository sellerRepository;

    @Transactional
    public BoardInfo createBoard(CreateBoardServiceCommand command) {

        List<ProductImg> productImgs = command.productImgs().stream()
            .map(ProductImgCommand::toProductImg)
            .toList();

        Board board = Board.sellerCreate(
            command.store(),
            command.title(),
            command.price(),
            command.discountType(),
            command.discountValue(),
            command.deliveryFee(),
            command.freeShippingConditions(),
            command.isFresh(),
            command.productionStartTime(),
            command.deliveryCondition(),
            command.deliveryCompany(),
            command.productInfoNotice().toProductInfoNotice(),
            command.boardDetail().toBoardDetail()
        );

        board.addProductImgs(productImgs);

        List<Product> products = command.products().stream()
            .map(productCommand -> productCommand.toProduct(board))
            .toList();
        board.addProducts(products);

        boardRepository.save(board);

        return BoardInfo.from(board);
    }

    @Transactional
    public BoardInfo updateBoard(UpdateBoardServiceCommand command) {
        Board board = boardRepository.findWithAllById(command.boardId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));

        validateOwnership(command.sellerId(), board);

        board.update(
            command.title(),
            command.price(),
            command.discountType(),
            command.discountValue(),
            command.deliveryFee(),
            command.freeShippingConditions(),
            command.isFresh(),
            command.productionStartTime(),
            command.deliveryCondition(),
            command.deliveryCompany()
        );

        board.getProductInfoNotice().update(
            command.productInfoNotice().productName(),
            command.productInfoNotice().foodType(),
            command.productInfoNotice().manufacturer(),
            command.productInfoNotice().originLocation(),
            command.productInfoNotice().manufactureDate(),
            command.productInfoNotice().expirationDate(),
            command.productInfoNotice().storageGuide(),
            command.productInfoNotice().packagingQuantityUnit(),
            command.productInfoNotice().rawMaterialName(),
            command.productInfoNotice().nutritionInfo(),
            command.productInfoNotice().transgenic(),
            command.productInfoNotice().customerWarning(),
            command.productInfoNotice().importFood()
        );

        board.getBoardDetail().update(command.boardDetail().content());

        mergeProducts(board, command.products());

        // dirty 상태(board, productInfoNotice, boardDetail, products) flush
        // softDeleteByBoardIds의 clearAutomatically = true로 인해 EM이 clear되기 전에 반영해야 함
        boardRepository.flush();

        // 기존 ProductImg soft-delete 후 새 이미지 저장
        productImgRepository.softDeleteByBoardIds(List.of(command.boardId()));
        List<ProductImg> newProductImgs = command.productImgs().stream()
            .map(ProductImgCommand::toProductImg)
            .toList();
        newProductImgs.forEach(img -> img.updateBoard(board));
        productImgRepository.saveAll(newProductImgs);

        return BoardInfo.from(board);
    }

    private void validateOwnership(Long sellerId, Board board) {
        Long storeIdOfSeller = sellerRepository.findStoreIdBySellerId(sellerId);
        if (storeIdOfSeller == null || !board.getStore().getId().equals(storeIdOfSeller)) {
            throw new BbangleException(BbangleErrorCode.FORBIDDEN_BOARD_ACCESS);
        }
    }

    private void mergeProducts(Board board, List<UpdateProductCommand> productCommands) {
        Map<Long, Product> existingMap = board.getProducts().stream()
            .filter(p -> !p.isDeleted())
            .collect(Collectors.toMap(Product::getId, p -> p));

        Set<Long> requestedIds = productCommands.stream()
            .filter(cmd -> cmd.productId() != null)
            .map(UpdateProductCommand::productId)
            .collect(Collectors.toSet());

        existingMap.values().stream()
            .filter(p -> !requestedIds.contains(p.getId()))
            .forEach(Product::delete);

        List<Product> newProducts = new ArrayList<>();
        for (UpdateProductCommand cmd : productCommands) {
            if (cmd.productId() != null && existingMap.containsKey(cmd.productId())) {
                existingMap.get(cmd.productId()).update(
                    cmd.title(),
                    cmd.plusPriceWithBoardPrice(),
                    cmd.category(),
                    cmd.stock(),
                    cmd.glutenFreeTag(),
                    cmd.highProteinTag(),
                    cmd.sugarFreeTag(),
                    cmd.veganTag(),
                    cmd.ketogenicTag(),
                    cmd.monday(),
                    cmd.tuesday(),
                    cmd.wednesday(),
                    cmd.thursday(),
                    cmd.friday(),
                    cmd.saturday(),
                    cmd.sunday(),
                    cmd.nutrition()
                );
            } else {
                newProducts.add(cmd.toNewProduct(board));
            }
        }

        board.addProducts(newProducts);
    }

}
