package com.bbangle.bbangle.image.customer.service;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.exception.BbangleException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

class ObjectStorageRepositoryTest extends AbstractIntegrationTest {

    private final ObjectStorageRepositoryImpl objectStorageRepository;
    private final String BUCKET_NAME = "bbangle-bucket";
    private final String STORE_ID = "1";
    private final String BOARD_ID = "2";
    private final String FOLDER_PATH = String.format("%s/%s/", STORE_ID, BOARD_ID);
    private final String HTML_FILE_NAME = "detail.html";
    private final String OBJECT_NAME = FOLDER_PATH + HTML_FILE_NAME;


    ObjectStorageRepositoryTest(
        @Autowired
        ObjectStorageRepositoryImpl objectStorageRepository
    ) {
        this.objectStorageRepository = objectStorageRepository;
    }

    @Test
    @DisplayName("ObjectStorage에 정상적으로 폴더를 만들 수 있다")
    void createStorageFolder() {
        objectStorageRepository.createFolder(BUCKET_NAME, FOLDER_PATH);
    }

    @Test
    @DisplayName("ObjectStorage에 정상적으로 파일을 저장할 수 있다")
    void createStorageFile() {
        // https://green-bin.tistory.com/112 S3 Mock 테스트 미래에 참조
        byte[] content;
        try {
            content = Files.readAllBytes(Paths.get("src/test/resources/html/detail.html"));
        } catch (IOException e) {
            throw new BbangleException(e);
        }

        // MockMultipartFile 인스턴스 생성
        MockMultipartFile HTML_FILE = new MockMultipartFile(
            "htmlFile", // form의 input field 이름
            "detail.html", // 업로드될 파일명
            "text/html", // 파일 타입
            content // 파일 내용
        );

        objectStorageRepository.createFile(BUCKET_NAME, OBJECT_NAME, HTML_FILE);
    }

}
