package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Store;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BoardFixture {

    public static Board randomBoardWithMoney(
        Store store,
        int price
    ) {
        List<Boolean> orderAvailable = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            int random = (int) (Math.random() + 1);
            boolean isAvailable = random == 1;
            orderAvailable.add(isAvailable);
        }

        return Board.builder()
            .store(store)
            .title(CommonFaker.faker.book().title())
            .price(price)
            .status(true)
            .purchaseUrl(CommonFaker.faker.internet().url())
            .isDeleted(false)
            .build();
    }

    public static Board randomBoard(
        Store store
    ) {
        List<Boolean> orderAvailable = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            int random = (int) (Math.random() + 1);
            boolean isAvailable = random == 1;
            orderAvailable.add(isAvailable);
        }
        int randomPrice = CommonFaker.faker.random()
            .nextInt(0, 100_000);
        return Board.builder()
            .store(store)
            .title(CommonFaker.faker.book().title())
            .price(randomPrice)
            .status(true)
            .purchaseUrl(CommonFaker.faker.internet().url())
            .isDeleted(false)
            .build();
    }

    public static Board randomBoardWithPrice(
        Store store,
        int price
    ) {
        List<Boolean> orderAvailable = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            int random = (int) (Math.random() + 1);
            boolean isAvailable = random == 1;
            orderAvailable.add(isAvailable);
        }
        return Board.builder()
            .store(store)
            .title(CommonFaker.faker.book().title())
            .price(price)
            .status(true)
            .purchaseUrl(CommonFaker.faker.internet().url())
            .isDeleted(false)
            .build();
    }

}
