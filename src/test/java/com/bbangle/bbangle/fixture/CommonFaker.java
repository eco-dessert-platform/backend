package com.bbangle.bbangle.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.datafaker.Faker;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonFaker {

    public static final Faker faker = new Faker();

}
