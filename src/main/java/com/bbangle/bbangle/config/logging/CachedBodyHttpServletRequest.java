package com.bbangle.bbangle.config.logging;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import org.springframework.util.StreamUtils;

/**
 * <p>Request 요청이 들어오면 Wrapper로 감싼다.</p>
 * <p>Wrapper로 감싸지 않고 필터에서 Request를 읽을 경우 Request 본문이 소비되어 소멸된다.</p>
 * <p>이후 서블릿에 Request가 전달되지 않는다.</p>
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    @Getter
    private final byte[] cachedBody;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        // 원본 Request 스트림을 읽어서 byte[]에 복사
        this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
    }

    @Override
    public ServletInputStream getInputStream() {
        // 매번 호출할 때마다 byte[]에 저장된 정보를 새로운 Request 스트림으로 만들어 전달
        return new CachedBodyServletInputStream(cachedBody);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    private static class CachedBodyServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream buffer;

        CachedBodyServletInputStream(byte[] contents) {
            this.buffer = new ByteArrayInputStream(contents);
        }

        @Override public boolean isFinished() { return buffer.available() == 0; }
        @Override public boolean isReady() { return true; }
        @Override public void setReadListener(ReadListener readListener) {}
        @Override public int read() { return buffer.read(); }
    }
}
