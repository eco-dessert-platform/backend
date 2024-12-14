package com.bbangle.bbangle.util;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.search.domain.TokenType;
import com.bbangle.bbangle.search.service.utils.Tokenizer;
import java.util.List;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
public class KomoranTokenizer implements Tokenizer {

    @Override
    public List<String> getAllTokenizer(String title) {
        return new Komoran(DEFAULT_MODEL.FULL).analyze(title)
            .getTokenList()
            .stream()
            .map(Token::getMorph)
            .toList();
    }

    @Override
    public List<String> getToken(TokenType tokenType, String title) {
        if (tokenType.equals(TokenType.NOUN)) {
            return getTokenizer(title).getMorphesByTags("NNG", "NNP", "NNB", "NP", "NR", "NA");
        }

        throw new BbangleException(BbangleErrorCode.INVALID_TOKEN_TYPE);
    }

    private KomoranResult getTokenizer(String title) {
        return new Komoran(DEFAULT_MODEL.FULL).analyze(title);
    }

}