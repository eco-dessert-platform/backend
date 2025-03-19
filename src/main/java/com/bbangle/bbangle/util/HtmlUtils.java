package com.bbangle.bbangle.util;

import com.bbangle.bbangle.config.CdnConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HtmlUtils {

    @Value("${cdn.domain}")
    private String cloudFrontUrl;

    public String convertHtmlWithFullImageUrls(String rawHtml) {
        // HTML 내 상대경로("BoardDetail/1/4.png")를 절대경로("https://d37g3q9mfan3cw.cloudfront.net/BoardDetail/1/4.png")로 변환
        return rawHtml.replaceAll("(<img src=\")([^\"].*?)(\")", "$1" + cloudFrontUrl + "/$2$3");
    }

    public static String convertHtmlWithFullImageUrls2(String rawHtml) {
        return rawHtml.replaceAll("(<img src=\")([^\"].*?)(\")", "$1" + CdnConfig.getCloudFrontUrl() + "/$2$3");
    }
}
