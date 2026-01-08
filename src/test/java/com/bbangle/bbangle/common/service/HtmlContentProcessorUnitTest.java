package com.bbangle.bbangle.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] HtmlContentProcessor")
class HtmlContentProcessorUnitTest {
    private final HtmlContentProcessor sut = new HtmlContentProcessor(null); // S3Service는 사용되지 않으므로 null 전달


}
