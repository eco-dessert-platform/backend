package com.bbangle.bbangle.config.logging.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;

/**
 * Body 데이터를 마스킹 처리 및 가독성을 높이기 위해 실제 JSON 형식으로 출력을 도와주는 유틸 클래스
 */
@NoArgsConstructor
public class JsonPrettyPrinterUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int BASE64_LENGTH_THRESHOLD = 200;

    private static final Pattern BASE64_PATTERN = Pattern.compile("^[A-Za-z0-9+/]+={0,2}$");
    private static final Pattern DATA_URI_PATTERN =
        Pattern.compile("^data:(image|video|audio)/[a-zA-Z0-9.+-]+;base64,.*", Pattern.CASE_INSENSITIVE);

    // 민감한 정보를 담은 JSON 필드 마스킹 - 소문자로 입력.
    private static final Set<String> SENSITIVE_FIELD_KEYWORDS = Set.of(
        "refreshtoken", "accesstoken",
        "password", "passwd", "pwd", "pw", "accountid",
        "secret",
        "accountnumber", "bankcode", "phonenumber", "email"
    );

    public static String prettyPrint(String raw) {
        if (raw == null || raw.isBlank()) {
            return "    null";
        }
        try {
            // 1. 문자열을 Tree 자료구조로 파싱
            JsonNode node = OBJECT_MAPPER.readTree(raw);
            // 2. Tree를 순회하며 마스킹 처리
            JsonNode sanitized = sanitize(node);
            // 3. 마스킹된 Tree를 들여쓰기 적용된 문자열로 재직렬화
            String pretty = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(sanitized);
            // 4. 로그 정렬용 들여쓰기 추가
            return indent(pretty, "    ");
        } catch (Exception e) {
            return "    (본문 파싱 실패 - 생략)";
        }
    }

    private static JsonNode sanitize(JsonNode node) {
        if (node.isObject()) {
            ObjectNode result = OBJECT_MAPPER.createObjectNode();
            node.fields().forEachRemaining(e -> {
                if (isSensitiveField(e.getKey())) {
                    result.put(e.getKey(), "***MASKED***");
                } else {
                    result.set(e.getKey(), sanitize(e.getValue()));
                }
            });
            return result;
        }
        if (node.isArray()) {
            ArrayNode result = OBJECT_MAPPER.createArrayNode();
            node.forEach(item -> result.add(sanitize(item)));
            return result;
        }
        if (node.isTextual() && isBase64Like(node.textValue())) {
            return new TextNode("(base64/media 데이터 생략, length=" + node.textValue().length() + ")");
        }
        return node;
    }

    // 민감한 정보를 담은 필드인지 검사
    private static boolean isSensitiveField(String fieldName) {
        if (fieldName == null) return false;
        String lower = fieldName.toLowerCase();
        return SENSITIVE_FIELD_KEYWORDS.stream().anyMatch(lower::contains);
    }

    // Base64 인코딩 미디어 파일 검사
    private static boolean isBase64Like(String text) {
        if (text == null) return false;
        if (DATA_URI_PATTERN.matcher(text).matches()) return true;
        return text.length() >= BASE64_LENGTH_THRESHOLD && BASE64_PATTERN.matcher(text).matches();
    }

    // JSON 형식 줄바꿈 처리
    private static String indent(String text, String indent) {
        return Arrays.stream(text.split("\n"))
            .map(line -> indent + line)
            .collect(Collectors.joining("\n"));
    }
}
