package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.store.domain.Store;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreFixture {

    public static  Store storeGenerator() {
        String randomIdentifier = String.valueOf(CommonFaker.faker.random()
            .nextLong(0, 10_000_000));

        return Store.builder()
            .identifier(randomIdentifier)
            .name(CommonFaker.faker.name().name())
            .introduce(CommonFaker.faker.movie().quote())
            .profile(CommonFaker.faker.internet().url())
            .isDeleted(false)
            .build();
    }

}
