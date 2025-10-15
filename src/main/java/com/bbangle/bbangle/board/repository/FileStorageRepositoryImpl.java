package com.bbangle.bbangle.board.repository;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;

import java.io.*;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileStorageRepositoryImpl implements FileStorageRepository {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final AmazonS3 amazonS3;

    @Override
    public InputStream getFile(String key) {
        try {
            S3Object s3Object = amazonS3.getObject(bucketName, key);
            return s3Object.getObjectContent();
        } catch (AmazonServiceException e) {
            throw new BbangleException(BbangleErrorCode.AWS_ERROR);
        } catch (NullPointerException e) {
            throw new BbangleException(BbangleErrorCode.NULL_FILE_URL);
        }
    }

    @Override
    public void uploadFile(InputStream file, String key, String contentType,
        long contentLength) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(contentLength);

        try {
            amazonS3.putObject(bucketName, key, file, metadata);
        } catch (AmazonServiceException e) {
            throw new BbangleException(BbangleErrorCode.AWS_ERROR);
        } finally {
            try {
                if (file != null) {
                    file.close();
                }
            } catch (IOException e) {
                throw new BbangleException(BbangleErrorCode.STREAM_CLOSING_ERROR, e);
            }
        }
    }

    @Override
    public void uploadFile(String path, File file) throws AmazonServiceException {
        amazonS3.putObject(bucketName, path, file);
    }

    @Override
    public boolean doesFileExist(String key) {
        try {
            return amazonS3.doesObjectExist(bucketName, key);
        } catch (SdkClientException e) {
            throw new BbangleException(BbangleErrorCode.AWS_CLIENT_ERROR);
        }
    }
}

