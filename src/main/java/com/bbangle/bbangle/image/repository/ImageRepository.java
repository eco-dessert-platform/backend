package com.bbangle.bbangle.image.repository;

import com.bbangle.bbangle.image.domain.Image;
import com.bbangle.bbangle.image.domain.ImageCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByImageCategoryAndDomainIdOrderByOrderAsc(
        @NonNull ImageCategory imageCategory,
        @NonNull Long domainId
    );

    List<Image> findAllByPathIn(List<String> urls);
}
