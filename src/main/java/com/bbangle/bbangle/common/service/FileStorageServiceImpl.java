package com.bbangle.bbangle.common.service;

import static com.bbangle.bbangle.exception.BbangleErrorCode.AWS_S3_FILE_NOT_FOUND;
import static com.bbangle.bbangle.exception.BbangleErrorCode.NOT_VALID_CONTENT_TYPE;
import static com.bbangle.bbangle.exception.BbangleErrorCode.NOT_VALID_FILE_SIZE;
import static com.bbangle.bbangle.exception.BbangleErrorCode.NULL_FILE_URL;
import static com.bbangle.bbangle.exception.BbangleErrorCode.NULL_INPUT_STREAM;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final AmazonS3 amazonS3;

    @Override
    public InputStream getFile(String savedUrl) {
        validateFileExists(savedUrl);

        try {
            S3Object s3Object = amazonS3.getObject(bucketName, savedUrl);
            return s3Object.getObjectContent();
        } catch (AmazonServiceException e) {
            log.error(e.getMessage(), e);
            throw new BbangleException(BbangleErrorCode.AWS_ERROR);
        } catch (SdkClientException e) {
            log.error(e.getMessage(), e);
            throw new BbangleException(BbangleErrorCode.AWS_CLIENT_ERROR);
        }
    }

    @Override
    public void uploadStreamFile(
        InputStream file,
        String savedUrl,
        String contentType,
        long contentLength
    ) {
        validFileUploading(file, savedUrl, contentType, contentLength);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);

        try {
            amazonS3.putObject(bucketName, savedUrl, file, metadata);
        } catch (AmazonServiceException e) {
            log.error(e.getMessage(), e);
            throw new BbangleException(BbangleErrorCode.AWS_ERROR);
        } catch (SdkClientException e) {
            log.error(e.getMessage(), e);
            throw new BbangleException(BbangleErrorCode.AWS_CLIENT_ERROR);
        } finally {
            if (Objects.nonNull(file)) {
                try {
                    file.close();
                } catch (IOException e) {
                    log.error("Failed to close the file stream: {}", e.getMessage(), e);
                }
            }
        }
    }


    private void validFileUploading(
        InputStream file,
        String savedUrl,
        String contentType,
        long contentLength
    ) {
        if (Objects.isNull(file)) throw new BbangleException(NULL_INPUT_STREAM);
        validateFileExists(savedUrl);
        if (Objects.isNull(contentType) || contentType.isEmpty()) throw new BbangleException(NOT_VALID_CONTENT_TYPE);
        if (contentLength <= 0) throw new BbangleException(NOT_VALID_FILE_SIZE);
    }

    private void validateFileExists(String savedUrl) {
        if (Objects.isNull(savedUrl) || savedUrl.trim().isEmpty()) {
            throw new BbangleException(NULL_FILE_URL);
        }

        if (!amazonS3.doesObjectExist(bucketName, savedUrl)) {
            throw new BbangleException(AWS_S3_FILE_NOT_FOUND);
        }
    }
}
