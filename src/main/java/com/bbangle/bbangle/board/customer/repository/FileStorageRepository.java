package com.bbangle.bbangle.board.customer.repository;

import com.amazonaws.AmazonServiceException;

import java.io.File;
import java.io.InputStream;

public interface FileStorageRepository {
    InputStream getFile(String key);

    void uploadFile(InputStream file, String key, String contentType, long contentLength) throws AmazonServiceException;

    void uploadFile(String path, File file) throws AmazonServiceException;

    boolean doesFileExist(String key);
}
