package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.search.repository.SearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FixtureConfig {

    @Autowired
    private SearchRepository searchRepository;
    @Autowired
    private ProductRepository productRepository;

    @Bean
    public SearchFixture searchFixture() {
        return new SearchFixture(searchRepository);
    }

    @Bean
    public ProductFixture productFixture() {
        return new ProductFixture(productRepository);
    }
}
