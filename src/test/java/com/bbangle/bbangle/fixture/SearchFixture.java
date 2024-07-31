package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.search.domain.Search;
import com.bbangle.bbangle.search.repository.SearchRepository;

public class SearchFixture {

    private final SearchRepository searchRepository;

    public SearchFixture(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    public Search create(Long memberId) {
        return searchRepository.save(Search.builder()
            .keyword(CommonFaker.faker.dessert().flavor())
            .memberId(memberId)
            .build());
    }
}
