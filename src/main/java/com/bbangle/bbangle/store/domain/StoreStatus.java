package com.bbangle.bbangle.store.domain;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreStatus {

    RESERVED("선점"),
    ACTIVE("등록"),
    NONE("비선점");


    private final String description;


    public static StoreStatus fromDescription(String desc) {
        return Arrays.stream(values())
            .filter(s -> s.getDescription().equals(desc))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown document type: " + desc));
    }


    }
