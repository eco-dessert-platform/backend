package com.bbangle.bbangle.config.logging;


import com.bbangle.bbangle.util.JsonPrettyPrinterUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import lombok.NoArgsConstructor;
import org.springframework.util.StreamUtils;

/**
 * Multipart 캡처 담당 유틸
 */
@NoArgsConstructor
public class MultipartBodyReader {

    /**
     * multipart 요청에서 파일이 아닌 파트(JSON, 폼 필드 등)만 읽어서 미리 캐싱한다.
     * 파일 파트는 내용을 읽지 않고 메타정보(파일명, 크기)만 남긴다.
     */
    public static String captureTextParts(HttpServletRequest request) {
        try {
            Collection<Part> parts = request.getParts();
            if (parts.isEmpty()) {
                return "    null";
            }

            StringBuilder sb = new StringBuilder();
            for (Part part : parts) {
                if (part.getSubmittedFileName() != null) {
                    sb.append(formatFilePart(part)).append("\n");
                } else {
                    sb.append(formatTextPart(part)).append("\n");
                }
            }

            return sb.toString().stripTrailing();
        } catch (Exception e) {
            return "    (multipart 파싱 실패 - 생략)";
        }
    }

    // MultiPart 메타 데이터 출력
    private static String formatFilePart(Part part) {
        return "    [%s] (file) fileName=%s, contentType=%s, size=%d bytes".formatted(
            part.getName(), part.getSubmittedFileName(), part.getContentType(), part.getSize());
    }

    private static String formatTextPart(Part part) throws IOException {
        String content = StreamUtils.copyToString(part.getInputStream(), StandardCharsets.UTF_8);
        return "    [%s]\n%s".formatted(part.getName(), JsonPrettyPrinterUtil.prettyPrint(content));
    }
}
