package com.bbangle.bbangle.review.dto;

import com.bbangle.bbangle.review.domain.Badge;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record ReviewRequest(
    @NotNull
    @Size(min = 3, max = 3)
    List<Badge> badges,
    @NotNull
    @DecimalMin(value = "0")
    @DecimalMax(value = "5")
    BigDecimal rate,
    String content,
    @NotNull
    Long boardId,
    List<String> photos
) {
}
