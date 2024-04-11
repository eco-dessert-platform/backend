package com.bbangle.bbangle.notice.repository;

import com.bbangle.bbangle.notice.dto.NoticeDetailResponseDto;
import com.bbangle.bbangle.notice.dto.NoticeResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeQueryDSLRepository {
    Page<NoticeResponseDto> getNoticeList(Pageable pageable);
    NoticeDetailResponseDto getNoticeDetail(Long id);
}
