package com.bbangle.bbangle.repository.impl;

import com.bbangle.bbangle.repository.SearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class SearchRepositoryTest {
    @Autowired
    SearchRepository searchRepository;

    @Test
    public void getBestKeywordTest(){
        String[] keywords = searchRepository.getBestKeyword();
        System.out.println(keywords);
    }
}
