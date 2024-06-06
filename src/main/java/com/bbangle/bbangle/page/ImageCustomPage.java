package com.bbangle.bbangle.page;

import com.bbangle.bbangle.review.dto.ReviewImgDto;

import java.util.List;

public class ImageCustomPage<T> extends CustomPage<T>{
    public ImageCustomPage(T content, Long requestCursor, Boolean hasNext) {
        super(content, requestCursor, hasNext);
    }

    public static ImageCustomPage<List<ReviewImgDto>> from(List<ReviewImgDto> content,
                                                           Long requestCursor,
                                                           Boolean hasNext){
        return new ImageCustomPage<>(content, requestCursor, hasNext);
    }
}
