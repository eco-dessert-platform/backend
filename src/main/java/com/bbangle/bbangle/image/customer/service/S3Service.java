package com.bbangle.bbangle.image.customer.service;

import static com.bbangle.bbangle.image.customer.validation.ImageValidator.validateImage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.bbangle.bbangle.exception.BbangleException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${bucket.domain}")
    private String bucketDomain;
    @Value("${cdn.domain}")
    private String cdnDomain;

    @Transactional
    public String saveImage(MultipartFile request) {
        validateImage(request);

        final String originName = request.getOriginalFilename();
        final String ext = originName.substring(originName.lastIndexOf("."));
        final String changedImageName = changeImageName(ext);
        return uploadImage(request, ext, changedImageName);
    }

    @Transactional
    public String saveAndReturnWithCdn(String folderName, MultipartFile image) {
        String imagePath = saveImage(image, folderName);
        log.debug("Show image path: {}", imagePath);
        imagePath = removeBucketDomainInFolder(imagePath);
        return addCdnDomain(imagePath);
    }

    @Transactional
    public String saveImage(MultipartFile request, String folder) {
        validateImage(request);

        final String originName = request.getOriginalFilename();
        final String ext = originName.substring(originName.lastIndexOf("."));
        final String changedImageName = folder + "/" + changeImageName(ext);
        return uploadImage(request, ext, changedImageName);
    }

    public CopyObjectResult copyImage(String fromPath, String toPath) {
        AccessControlList accessControlList = new AccessControlList();
        accessControlList.grantPermission(GroupGrantee.AllUsers, Permission.Read);
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(
            bucket,
            fromPath,
            bucket,
            toPath)
            .withAccessControlList(accessControlList);
        return amazonS3.copyObject(copyObjectRequest);
    }

    public void deleteImages(List<String> urls) {
        urls.forEach(url -> amazonS3.deleteObject(bucket, url));
    }

    private String uploadImage(
        final MultipartFile image,
        final String ext,
        final String changedImageName
    ) {

        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/" + ext.substring(1));
        // [추가] 파일 크기 명시 (메모리 효율 및 WARN 해결)
        metadata.setContentLength(image.getSize());

        try {
            amazonS3.putObject(new PutObjectRequest(
                bucket, changedImageName, image.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (final IOException e) {
            throw new BbangleException("저장하기 유효하지 않은 이미지입니다.", e);
        }

        return amazonS3.getUrl(bucket, changedImageName)
            .toString();
    }

    private String changeImageName(final String ext) {
        final String uuid = UUID.randomUUID()
            .toString();
        return uuid + ext;
    }

    public @NotNull String removeBucketDomainInFolder(String imagePath) {
        String path = imagePath.replace(bucketDomain, "");
        if (path.startsWith("/")) {
            return path.substring(1); // 맨 앞 슬래시 제거
        }
        return imagePath.replace(bucketDomain, "");
    }

    public String addCdnDomain(String url) {
        // cdnDomain이 '/'로 끝나는지 확인
        boolean cdnHasSlash = cdnDomain.endsWith("/");
        // url이 '/'로 시작하는지 확인
        boolean urlHasSlash = url.startsWith("/");

        if (cdnHasSlash && urlHasSlash) {
            return cdnDomain + url.substring(1); // 둘 다 있으면 하나 제거
        } else if (!cdnHasSlash && !urlHasSlash) {
            return cdnDomain + "/" + url; // 둘 다 없으면 하나 추가
        } else {
            return cdnDomain + url; // 하나만 있으면 그냥 결합
        }
    }
}
