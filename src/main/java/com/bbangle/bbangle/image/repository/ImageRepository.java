package com.bbangle.bbangle.image.repository;

import com.bbangle.bbangle.image.domain.Image;
import com.bbangle.bbangle.image.domain.ImageCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByImageCategoryAndDomainIdOrderByOrderAsc(
        @NonNull ImageCategory imageCategory,
        @NonNull Long domainId
    );

    List<Image> findAllByPathIn(List<String> urls);

    List<Image> findByDomainId(Long reviewId);

    @Modifying
    @Query("delete from Image i where i.path in :imagePaths")
    void deleteAllByPathIn(@Param("imagePaths") List<String> imagePaths);

    Image findByPath(String url);
}
