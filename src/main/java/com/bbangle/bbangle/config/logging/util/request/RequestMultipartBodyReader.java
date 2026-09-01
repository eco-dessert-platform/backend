package com.bbangle.bbangle.config.logging.util.request;


import com.bbangle.bbangle.config.logging.util.JsonPrettyPrinterUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import lombok.experimental.UtilityClass;
import org.springframework.util.StreamUtils;


/**
 * Multipart 캡처 담당 유틸
 * 파일 파트는 읽지 않고(캐싱 비용 방지), JSON으로 넘어온 non-file 파트만 로그용으로 캡처한다.
 */
@UtilityClass
public class RequestMultipartBodyReader {

    /**
     * multipart 요청에서 파일이 아닌 파트(JSON, 폼 필드 등)만 읽어서 미리 캐싱한다.
     * 파일 파트는 컨트롤러 처리 후 임시 파일이 삭제될 수 있고 로깅 필요성도 낮으므로 아예 읽지 않고 생략한다.
     * @return non-file 파트가 없으면 null
     */
    public String captureJsonParts(HttpServletRequest request) {
        try {
            Collection<Part> parts = request.getParts();
            StringBuilder sb = new StringBuilder();
            for (Part part : parts) {
                if (part.getSubmittedFileName() != null) {
                    continue;
                }
                sb.append(formatJsonPart(part)).append("\n");
            }

            return sb.isEmpty() ? null : sb.toString().stripTrailing();
        } catch (Exception e) {
            return "    (multipart 파싱 실패 - 생략)";
        }
    }

    private String formatJsonPart(Part part) throws IOException {
        String content = StreamUtils.copyToString(part.getInputStream(), StandardCharsets.UTF_8);
        return JsonPrettyPrinterUtil.prettyPrint(content);
    }
}
