package com.bbangle.bbangle.board.seller.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.InventoryStatus;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.board.domain.SaleStatus;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductImgRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.board.repository.dao.SellerBoardDao;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.BoardUpdateDTO;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse;
import com.bbangle.bbangle.board.seller.controller.mapper.SellerBoardDetailMapper;
import com.bbangle.bbangle.board.seller.service.command.CreateBoardServiceCommand;
import com.bbangle.bbangle.board.seller.service.command.ProductImgCommand;
import com.bbangle.bbangle.board.seller.service.command.SearchSellerBoardCommand;
import com.bbangle.bbangle.board.seller.service.command.UpdateBoardServiceCommand;
import com.bbangle.bbangle.board.seller.service.command.UpdateProductCommand;
import com.bbangle.bbangle.board.seller.service.info.BoardInfo;
import com.bbangle.bbangle.board.seller.service.info.SellerBoardItemInfo;
import com.bbangle.bbangle.board.seller.service.info.SellerBoardListInfo;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.util.TitleDuplicatorUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerBoardService {

    private final BoardRepository boardRepository;
    private final ProductImgRepository productImgRepository;
    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final SellerBoardDetailMapper sellerBoardDetailMapper;

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

        // 요청된 상품 중 재고가 채워진 것이 있으면, 품절(OUT_OF_STOCK)로 인해 내려가 있던
        // 판매 상태를 자동으로 판매중(ON_SALE)으로 되돌린다. (판매자가 직접 STOPPED한 경우는 영향받지 않음)
        if (hasRestockedProduct(command.products())) {
            board.restock();
        }

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

    /**
     * 요청된 상품 목록 중 재고(stock)가 1 이상인 상품이 하나라도 있는지 확인한다.
     * 재고가 채워졌다는 것은 곧 판매 재개가 가능해졌다는 신호이므로,
     * OUT_OF_STOCK 상태의 게시글을 ON_SALE로 되돌리는 트리거로 사용한다.
     */
    private boolean hasRestockedProduct(List<UpdateProductCommand> productCommands) {
        return productCommands.stream().anyMatch(cmd -> cmd.stock() >= 1);
    }

    public SellerBoardListInfo searchBoards(SearchSellerBoardCommand command) {
        Seller seller = sellerRepository.findById(command.sellerId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.SELLER_NOT_FOUND));
        if (seller.getStore() == null) {
            throw new BbangleException(BbangleErrorCode.STORE_NOT_FOUND);
        }
        Long storeId = seller.getStore().getId();

        PageRequest pageable = PageRequest.of(command.page(), command.size());

        Page<SellerBoardDao> boardPage = boardRepository.findSellerBoards(
            storeId,
            command.saleStatus(),
            command.mainCategory(),
            command.category(),
            command.keyword(),
            command.sortBy(),
            pageable
        );

        List<Long> boardIds = boardPage.getContent().stream()
            .map(SellerBoardDao::boardId)
            .toList();

        Map<Long, InventoryStatus> inventoryStatusMap =
            productRepository.findInventoryStatusByBoardIds(boardIds);

        List<SellerBoardItemInfo> items = boardPage.getContent().stream()
            .map(dao -> SellerBoardItemInfo.of(
                dao,
                inventoryStatusMap.getOrDefault(dao.boardId(), InventoryStatus.IN_STOCK)
            ))
            .toList();

        Page<SellerBoardItemInfo> resultPage =
            new PageImpl<>(items, pageable, boardPage.getTotalElements());

        Map<SaleStatus, Long> tabCounts = boardRepository.countSellerBoardsBySaleStatus(storeId);

        return new SellerBoardListInfo(resultPage, tabCounts);
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
            if (cmd.productId() == null) {
                newProducts.add(cmd.toNewProduct(board));
            } else if (existingMap.containsKey(cmd.productId())) {
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
                throw new BbangleException(BbangleErrorCode.PRODUCT_NOT_FOUND);
            }
        }

        board.addProducts(newProducts);
    }

    /**
     * 판매자 상품 게시글 상세 조회.
     */
    public SellerBoardDetailResponse getBoardDetail(Long sellerId, Long boardId) {
        Board board = boardRepository.findByIdAndIsDeletedFalse(boardId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));

        if (!sellerRepository.existsByIdAndStore_IdAndIsDeletedFalse(sellerId, board.getStore().getId())) {
            throw new BbangleException(BbangleErrorCode.FORBIDDEN_BOARD_ACCESS);
        }

        List<ProductImg> productImgs = productImgRepository.findAllByBoardIdAndIsDeletedFalseOrderByImgOrderAsc(boardId);
        List<Product> products = productRepository.findAllByBoardIdAndIsDeletedFalse(boardId);

        return sellerBoardDetailMapper.toResponse(board, productImgs, products);
    }

    /**
     * 판매자가 명시적으로 게시글의 판매 상태(SaleStatus)를 변경한다.
     * - STOPPED: ON_SALE 또는 OUT_OF_STOCK 상태에서만 가능 (판매 중지)
     * - ON_SALE: STOPPED 상태에서만 가능 (판매 재개)
     * - 그 외 targetStatus(PENDING, BANNED, OUT_OF_STOCK)는 허용하지 않는다.
     *
     * @return 변경된 판매 상태를 담은 응답 DTO
     */
    @Transactional
    public BoardUpdateDTO changeSaleStatus(Long sellerId, Long boardId, SaleStatus targetStatus) {
        Board board = boardRepository.findByIdAndIsDeletedFalse(boardId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));

        if (!sellerRepository.existsByIdAndStore_IdAndIsDeletedFalse(sellerId, board.getStore().getId())) {
            throw new BbangleException(BbangleErrorCode.FORBIDDEN_BOARD_ACCESS);
        }

        switch (targetStatus) {
            case STOPPED -> board.stopSale();
            case ON_SALE -> board.resumeSale();
            default -> throw new BbangleException(BbangleErrorCode.INVALID_BOARD_STATUS);
        }

        return BoardUpdateDTO.builder()
            .boardId(board.getId())
            .name(board.getTitle())
            .status(board.getSaleStatus())
            .build();
    }

    /**
     * 게시글(Board)을 연관 엔티티(ProductImg, Product/Nutrition, BoardDetail, ProductInfoNotice)와 함께 그대로 복제한다.
     * <p>
     * - saleStatus는 PENDING(승인 대기)으로 초기화된다. <br>
     * - title은 같은 스토어 내 기존 제목들을 조회하여 "제목 (n)" 형태로 겹치지 않게 생성한다.
     *
     * @param sellerId 복제를 요청한 판매자 ID (소유권 검증용)
     * @param boardId  복제할 원본 게시글 ID
     * @return 새로 생성된 게시글의 상세 응답
     */
    @Transactional
    public SellerBoardDetailResponse copyBoard(Long sellerId, Long boardId) {
        Board originalBoard = boardRepository.findByIdAndIsDeletedFalse(boardId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));

        if (!sellerRepository.existsByIdAndStore_IdAndIsDeletedFalse(sellerId, originalBoard.getStore().getId())) {
            throw new BbangleException(BbangleErrorCode.FORBIDDEN_BOARD_ACCESS);
        }

        List<Product> originalProducts = productRepository.findAllByBoardIdAndIsDeletedFalse(boardId);
        List<ProductImg> originalImgs = productImgRepository.findAllByBoardIdAndIsDeletedFalseOrderByImgOrderAsc(boardId);

        List<String> existingTitles = boardRepository.findAllTitlesByStoreId(originalBoard.getStore().getId());
        String newTitle = TitleDuplicatorUtil.generateNextTitle(originalBoard.getTitle(), existingTitles);

        Board copiedBoard = Board.copyOf(originalBoard, newTitle);

        List<Product> copiedProducts = originalProducts.stream()
            .map(Product::copyOf)
            .toList();
        copiedBoard.addProducts(copiedProducts);

        List<ProductImg> copiedImgs = originalImgs.stream()
            .map(ProductImg::copyOf)
            .toList();
        copiedBoard.addProductImgs(copiedImgs);

        boardRepository.save(copiedBoard);

        return sellerBoardDetailMapper.toResponse(copiedBoard, copiedImgs, copiedProducts);
    }
}
