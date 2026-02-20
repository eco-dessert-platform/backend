package com.bbangle.bbangle.board.domain;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    ALL("전체", null),
    BREAD("식빵/모닝빵", MainCategory.BREAD),
    BAGEL("베이글/도넛", MainCategory.BREAD),
    CAKE("케이크", MainCategory.BREAD),
    COOKIE("쿠키/비스킷/크래커", MainCategory.SNACK),
    JAM("잼/청", MainCategory.SNACK),
    GRANOLA("그래놀라", MainCategory.SNACK),
    TART("타르트/파이", MainCategory.BREAD),
    SNACK("과자", MainCategory.SNACK),
    ICE_CREAM("아이스크림", MainCategory.SNACK),
    YOGURT("요거트", MainCategory.SNACK),
    ETC("기타", null);

    private final String description;
    private final MainCategory mainCategory;

    public static boolean checkCategory(String category) {
        return Arrays.stream(Category.values())
            .anyMatch(e -> e.name()
                .equals(category));
    }

    public static Category from(String value) {
        return Arrays.stream(values())
            .filter(category -> category.name().equals(value))
            .findFirst()
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.INVALID_PRODUCT_CATEGORY));
    }

    public static List<Category> findByMainCategory(MainCategory mainCategory) {
        return Arrays.stream(Category.values())
            .filter(c -> c.mainCategory == mainCategory)
            .toList();
    }
}
