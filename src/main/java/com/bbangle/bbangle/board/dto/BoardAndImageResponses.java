package com.bbangle.bbangle.board.dto;

import java.util.List;

public record BoardAndImageResponses(
        Long boardId,
        Long storeId,
        String profile,
        String title,
        Integer price,
        String purchaseUrl,
        Boolean status,
        Integer deliveryFee,
        Integer freeShippingConditions,
        int discountRate,
        List<String> boardImagesWithoutThumbnail
) {
    public static BoardAndImageResponses createFromDtos(List<BoardAndImageDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            throw new IllegalArgumentException("DTO 리스트가 비어 있습니다.");
        }

        BoardAndImageDto first = extractCommonInfo(dtos);
        String profile = extractThumbnailImages(dtos);
        List<String> boardImagesWithoutThumbnail = extractNonThumbnailImages(dtos);

        return new BoardAndImageResponses(
                first.boardId(),
                first.storeId(),
                profile,
                first.title(),
                first.price(),
                first.purchaseUrl(),
                first.status(),
                first.deliveryFee(),
                first.freeShippingConditions(),
                first.discountRate(),
                boardImagesWithoutThumbnail
        );
    }

    private static BoardAndImageDto extractCommonInfo(List<BoardAndImageDto> dtos) {
        return dtos.get(0);
    }

    private static String extractThumbnailImages(List<BoardAndImageDto> dtos) {
        return dtos.stream()
                .filter(dto -> dto.isWithThumbNailImage())
                .map(BoardAndImageDto::profile)
                .findFirst()
                .orElseThrow();
    }

    private static List<String> extractNonThumbnailImages(List<BoardAndImageDto> dtos) {
        return dtos.stream()
                .filter(dto -> !dto.isWithThumbNailImage())
                .map(BoardAndImageDto::profile)
                .toList();
    }
}
