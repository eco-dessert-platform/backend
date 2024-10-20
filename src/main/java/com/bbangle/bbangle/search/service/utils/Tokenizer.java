package com.bbangle.bbangle.search.service.utils;

import com.bbangle.bbangle.search.domain.TokenType;
import java.util.List;

public interface Tokenizer {
    List<String> getAllTokenizer(String title);
    List<String> getToken(TokenType tokenType, String title);
}