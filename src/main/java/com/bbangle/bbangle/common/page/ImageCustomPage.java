package com.bbangle.bbangle.common.page;

import com.bbangle.bbangle.image.customer.dto.ImageDto;
import java.util.List;

public class ImageCustomPage<T> extends CustomPage<T> {

    public ImageCustomPage(T content, Long requestCursor, Boolean hasNext) {
        super(content, requestCursor, hasNext);
    }

    public static ImageCustomPage<List<ImageDto>> from(List<ImageDto> content,
        Long requestCursor,
        Boolean hasNext) {
        return new ImageCustomPage<>(content, requestCursor, hasNext);
    }
}
