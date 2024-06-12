package com.bbangle.bbangle.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.bbangle.bbangle.exception.BbangleException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

@Repository
@RequiredArgsConstructor
class ObjectStorageRepositoryImpl {

    private final AmazonS3 s3;

    public void createFile(String bucketName, String objectName, MultipartFile file) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType()); // 콘텐츠 타입 설정
        objectMetadata.setContentLength(file.getSize()); // 파일 크기 설정
        try {
            s3.putObject(bucketName, objectName, file.getInputStream(), objectMetadata); // S3 저장
        } catch (IOException e) {
            throw new BbangleException(e);
        }
    }


    public void createFolder(String bucketName, String folderName) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(0L);
        objectMetadata.setContentType("application/x-directory");
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, folderName,
            new ByteArrayInputStream(new byte[0]), objectMetadata);
        s3.putObject(putObjectRequest);
    }
}
