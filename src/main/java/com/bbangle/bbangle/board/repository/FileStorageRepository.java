package com.bbangle.bbangle.board.repository;

import java.io.InputStream;

public interface FileStorageRepository {
    InputStream getFile(String key);

    void uploadFile(InputStream file, String key, String contentType, long contentLength);

    boolean doesFileExist(String key);
}
