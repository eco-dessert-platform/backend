package com.bbangle.bbangle.image.repository;

import com.bbangle.bbangle.image.domain.Image;
import com.bbangle.bbangle.image.domain.ImageCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByImageCategoryAndDomainIdOrderByOrderAsc(
        @NonNull ImageCategory imageCategory,
        @NonNull Long domainId
    );

    List<Image> findAllByPathIn(List<String> urls);

    List<Image> findByDomainId(Long reviewId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Image i WHERE i.path IN :imagePaths")
    void deleteAllByPathIn(@Param("imagePaths") List<String> imagePaths);

    Image findByPath(String url);

    @Modifying
    @Query("DELETE FROM Image i WHERE i.domainId = :tempDomainId")
    @Transactional
    void deleteTempImages(@Param("tempDomainId") Long tempDomainId);

    void deleteAllByDomainId(Long reviewId);

}
