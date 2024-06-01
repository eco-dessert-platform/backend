package com.bbangle.bbangle.image.service;

import static java.util.Locale.ROOT;

import com.bbangle.bbangle.image.domain.Image;
import com.bbangle.bbangle.image.domain.ImageCategory;
import com.bbangle.bbangle.image.repository.ImageRepository;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Service s3Service;
    private final ImageRepository imageRepository;

    /**
     * @return 간혹 이미지 저장된 url 이 필요한 곳이 있어서 url 리턴
     */
    public String save(
        ImageCategory category,
        MultipartFile image,
        long domainId
    ) {
        return save(category, image, domainId, 0);
    }

    public String save(
        ImageCategory category,
        MultipartFile image,
        long domainId,
        int order
    ) {
        String imagePath = s3Service.saveImage(image, imageFolderPathResolver(category, domainId));

        Image entity = createEntity(category, image, domainId, imagePath, order);
        imageRepository.save(entity);

        return imagePath;
    }

    public List<String> saveAll(
        ImageCategory category,
        List<MultipartFile> imageList,
        long domainId
    ) {
        AtomicInteger order = new AtomicInteger(0);

        List<Image> entities = imageList.stream().map(image -> {
            String imagePath = s3Service.saveImage(
                image,
                imageFolderPathResolver(category, domainId)
            );

            return createEntity(category, image, domainId, imagePath, order.getAndIncrement());
        }).toList();

        imageRepository.saveAll(entities);

        return entities.stream().map(Image::getPath).toList();
    }

    public List<String> findImagePathById(ImageCategory category, long domainId) {
        return imageRepository.findByImageCategoryAndDomainIdOrderByOrderAsc(category, domainId)
            .stream()
            .map(Image::getPath)
            .toList();
    }

    private Image createEntity(
        ImageCategory category,
        MultipartFile image,
        Long domainId,
        String imagePath,
        int order
    ) {
        return Image.builder()
            .imageCategory(category)
            .domainId(domainId)
            .fileName(image.getOriginalFilename())
            .path(imagePath)
            .order(order)
            .build();
    }

    private String imageFolderPathResolver(ImageCategory category, Long domainId) {
        return String.format(
            "/%s/%d",
            category.name().toLowerCase(ROOT),
            domainId
        );
    }
}
