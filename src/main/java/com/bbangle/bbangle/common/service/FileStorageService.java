package com.bbangle.bbangle.common.service;

import java.io.InputStream;

public interface FileStorageService {
    public InputStream getFile(String key);

    /*
    * Stream 방식
    * - 파일 용량이 적을 때 사용
    * */
    void uploadStreamFile(
        InputStream file,
        String savedUrl,
        String contentType,
        long contentLength
    );
}
