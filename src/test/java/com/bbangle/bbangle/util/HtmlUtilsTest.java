package com.bbangle.bbangle.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HtmlUtilsTest {

    @Autowired
    HtmlUtils htmlUtils;

    @Test
    void testConvertHtmlWithFullImageUrls() {
        // Given: 기존 HTML (상대 경로 사용)
        String rawHtml = """
                <html><body>
                    <img src="BoardDetail/1/4.png">
                    <img src="BoardDetail/1/5.png">
                    <img src="BoardDetail/1/6.png">
                    <img src="BoardDetail/1/7.png">
                    <img src="BoardDetail/1/8.png">
                </body></html>
                """;

        // Expected 변환된 HTML (CloudFront URL이 추가된 절대경로)
        String expectedHtml = """
                <html><body>
                    <img src="https://d37g3q9mfan3cw.cloudfront.net/BoardDetail/1/4.png">
                    <img src="https://d37g3q9mfan3cw.cloudfront.net/BoardDetail/1/5.png">
                    <img src="https://d37g3q9mfan3cw.cloudfront.net/BoardDetail/1/6.png">
                    <img src="https://d37g3q9mfan3cw.cloudfront.net/BoardDetail/1/7.png">
                    <img src="https://d37g3q9mfan3cw.cloudfront.net/BoardDetail/1/8.png">
                </body></html>
                """;

        // When: 변환 실행
        String resultHtml = htmlUtils.convertHtmlWithFullImageUrls(rawHtml);

        // Then: 변환된 HTML이 기대한 값과 일치하는지 검증
        assertThat(resultHtml).isEqualToIgnoringWhitespace(expectedHtml);
    }
}

