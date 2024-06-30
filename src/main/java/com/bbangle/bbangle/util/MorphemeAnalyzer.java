package com.bbangle.bbangle.util;

import java.util.List;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MorphemeAnalyzer {

    private static final Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);

    public List<String> getAllTokenizer(String title) {
        return komoran.analyze(title)
            .getTokenList()
            .stream()
            .map(Token::getMorph)
            .toList();
    }

    public List<String> getNounTokenizer(String title) {
        return getTokenizer(title).getMorphesByTags("NNG", "NNP", "NNB", "NP", "NR", "NA");
    }

    private KomoranResult getTokenizer(String title) {
        return komoran.analyze(title);
    }

}