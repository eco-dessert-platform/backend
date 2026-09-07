package com.bbangle.bbangle.config.logging.util.response;

import com.bbangle.bbangle.config.logging.util.HttpLogSupportUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import lombok.Getter;

/**
 * Response Body를 무조건 캐싱하지 않고, 최초로 OutputStream/Writer를 얻는 시점의
 * Content-Type을 보고 로깅 가능한 타입(JSON 등)일 때만 캐싱한다.
 *
 * <p>캐싱을 건너뛰는(=원본 스트림에 바로 쓰는) 대표 케이스:</p>
 * <ul>
 *     <li>zip, excel(xlsx) 등 파일 다운로드 — 컨트롤러가 body를 쓰기 전에
 *         {@code response.setContentType(...)}으로 non-JSON 타입을 먼저 지정하는 경우.
 *         파일 크기가 아무리 커도 메모리에 전체를 들고 있지 않는다.</li>
 *     <li>이미지 등 미디어 응답 — 마찬가지로 Content-Type이
 *         {@code application/json}, {@code text/*} 등에 해당하지 않는 경우.</li>
 *     <li>{@code StreamingResponseBody} 같은 비동기 응답 — 컨트롤러가 리턴한 뒤 별도 스레드에서
 *         실제 쓰기가 이루어지므로, 필터의 finally 블록이 실행되는 시점엔 아직 이 클래스의
 *         getOutputStream()/getWriter()가 한 번도 호출되지 않았을 수 있다. 이 경우 캐싱 여부
 *         판단({@code decided})이 내려지지 않은 채로 남아 {@link #isBodyCached()}가
 *         기본값 false를 반환 → 결과적으로 "캐싱 안 됨"과 동일하게 처리된다.</li>
 * </ul>
 *
 * <p>반대로, 대부분의 {@code @RestController}/{@code ResponseEntity} JSON 응답은 Spring의
 * HttpMessageConverter가 body를 쓰기 직전에 Content-Type 헤더를 응답에 반영하므로
 * 이 시점에 이미 Content-Type이 확정되어 있어 정상적으로 캐싱된다.</p>
 */
public class SelectiveCachingResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream cacheBuffer = new ByteArrayOutputStream();
    private ServletOutputStream outputStream;
    private PrintWriter writer;

    // 캐싱 여부(JSON 등 로깅 가능한 타입인지)를 담는 결과값
    @Getter
    private boolean bodyCached;

    // decideCachingIfNeeded()가 한 번이라도 실행됐는지 여부.
    // getOutputStream()/getWriter()가 아예 호출되지 않은 채 요청이 끝나면(비동기 스트리밍 등)
    // false로 남고, 이때 bodyCached도 초기값 false 그대로라 "캐싱 안 됨"과 동일하게 취급된다.
    private boolean decided;

    public SelectiveCachingResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            decideCachingIfNeeded();
            // 캐싱 대상이면 버퍼링 스트림, 아니면 원본 스트림에 즉시 write (메모리 사용 없음)
            outputStream = bodyCached ? new CachingServletOutputStream() : super.getOutputStream();
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            decideCachingIfNeeded();
            writer = bodyCached
                ? new PrintWriter(new OutputStreamWriter(new CachingServletOutputStream(), resolveEncoding()))
                : super.getWriter();
        }
        return writer;
    }

    /**
     * 캐싱 여부는 딱 한 번만 결정한다(최초 getOutputStream/getWriter 호출 시점).
     * 이 시점의 Content-Type을 기준으로 판단하므로, body를 쓰기 전에 Content-Type이
     * 먼저 세팅되어 있어야 정확히 동작한다 (대부분의 Spring MVC 응답 패턴이 이를 만족).
     */
    private void decideCachingIfNeeded() {
        if (!decided) {
            bodyCached = HttpLogSupportUtil.isLoggableContentType(getContentType());
            decided = true;
        }
    }

    private String resolveEncoding() {
        String encoding = getCharacterEncoding();
        return encoding != null ? encoding : StandardCharsets.UTF_8.name();
    }

    public byte[] getCachedBody() {
        return cacheBuffer.toByteArray();
    }

    /**
     * 캐싱된 경우(JSON 등)에만 버퍼에 모아둔 내용을 실제 응답 스트림으로 흘려보낸다.
     * 캐싱하지 않은 경우(zip/xlsx/이미지 등)는 write() 호출 시점에 이미 원본 스트림으로
     * 직접 전달됐으므로 여기서 할 일이 없다.
     */
    public void copyBodyToResponse() throws IOException {
        if (writer != null) {
            writer.flush();
        }

        if (bodyCached && cacheBuffer.size() > 0) {
            getResponse().getOutputStream().write(cacheBuffer.toByteArray());
        }
    }

    // JSON 등 캐싱 대상일 때만 사용되는, 버퍼에만 쓰고 원본 스트림엔 쓰지 않는 OutputStream
    private class CachingServletOutputStream extends ServletOutputStream {
        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }

        @Override
        public void write(int b) {
            cacheBuffer.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            cacheBuffer.write(b, off, len);
        }
    }
}
