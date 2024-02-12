package com.bbangle.bbangle.repository.impl;

import com.bbangle.bbangle.dto.StoreDetailResponseDto;
import com.bbangle.bbangle.repository.InitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class InitRepositoryTest {

    @Autowired
    InitRepository initRepository;

    @Test
    public void getStoreDetailResponseDtoTest(){
        var boardMap = initRepository.getAllBoardTitle();

        boardMap.entrySet().forEach(longStringEntry -> {
            System.out.println(longStringEntry.getKey() + longStringEntry.getValue());
        });
    }
}
