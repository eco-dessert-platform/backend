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

    /**
     *
     * 도메인 id 없이 임시로 저장이 필요할 때가 있어서 만듬
     * (추후 팀원들 논의 되면 리팩토링 예정)
     */
    public List<String> saveAll(
        ImageCategory category,
        List<MultipartFile> images
    ){
        return saveAll(category, images, -1);
    }

    public void move(String fromPath, String toPath){
        s3Service.copyImage(fromPath, toPath);
    }

    public String save(
        ImageCategory category,
        MultipartFile image,
        long domainId,
        int order
    ) {
        String imagePath = s3Service.saveImage(image, imageFolderPathResolver(category, domainId));

        Image entity = createEntity(category, domainId, imagePath, order);
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

            return createEntity(category, domainId, imagePath, order.getAndIncrement());
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
        Long domainId,
        String imagePath,
        int order
    ) {
        return Image.builder()
            .imageCategory(category)
            .domainId(domainId)
            .path(imagePath)
            .order(order)
            .build();
    }

    private String imageFolderPathResolver(ImageCategory category, Long domainId) {
        return String.format(
            "%s/%d",
            category.name().toLowerCase(ROOT),
            domainId
        );
    }
}
