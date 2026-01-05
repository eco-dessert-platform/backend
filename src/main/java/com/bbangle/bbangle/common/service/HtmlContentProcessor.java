package com.bbangle.bbangle.common.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.customer.service.S3Service;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HtmlContentProcessor {

     private final S3Service s3Service;

    /**
     * HTML content 내의 img 태그를 파싱하고 원본 src를 CDN URL로 교체합니다.
     * HTML의 img 개수가 originalSrcs 개수와 정확히 일치해야 합니다.
     *
     * @param content HTML 문자열
     * @param cdnUrlMap CDN URL 맵
     * @return 처리된 HTML 문자열
     * @throws BbangleException HTML img 개수가 originalSrcs 개수와 일치하지 않을 경우
     * @throws BbangleException 매칭되지 않은 img 태그가 있을 경우
     */
    public String changeToCdn(String content, Map<String,String> cdnUrlMap) {
        // Jsoup을 사용하여 HTML 안전하게 파싱
        Document doc = Jsoup.parse(content);
        doc.outputSettings().prettyPrint(false);
        Elements imgTags = doc.select("img[data-id]");

        // 각 img 태그의 src를 확인하여 원본 src와 매칭되면 CDN URL로 교체
        for (Element img : imgTags) {
            String uuid = img.attr("data-id");
            img.attr("src",cdnUrlMap.get(uuid));
        }
        return doc.body().html();
    }

}
