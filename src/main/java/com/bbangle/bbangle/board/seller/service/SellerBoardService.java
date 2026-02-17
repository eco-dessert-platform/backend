package com.bbangle.bbangle.board.seller.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.seller.service.command.CreateBoardServiceCommand;
import com.bbangle.bbangle.board.seller.service.command.ProductImgCommand;
import com.bbangle.bbangle.board.seller.service.info.BoardInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerBoardService {

    private final BoardRepository boardRepository;

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
            command.boardDetail().toBoardDetail(),
            productImgs
        );

        List<Product> products = command.products().stream()
            .map(productCommand -> productCommand.toProduct(board))
            .toList();
        board.addProducts(products);

        boardRepository.save(board);

        return BoardInfo.from(board);
    }

}
