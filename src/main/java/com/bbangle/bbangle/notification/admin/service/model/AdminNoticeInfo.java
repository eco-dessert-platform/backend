package com.bbangle.bbangle.notification.admin.service.model;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.notification.admin.controller.dto.LinkDto;
import com.bbangle.bbangle.notification.domain.Notice;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

public class AdminNoticeInfo {

    @Builder
    public record NoticeInfo(Long id,
                             String title,
                             String content,
                             List<LinkDto> links,
                             List<String> imageLinks,
                             LocalDateTime createAt,
                             LocalDateTime modifiedAt) {


        public static NoticeInfo from(Notice notice, ObjectMapper objectMapper) {
            try {
                List<LinkDto> linksList = notice.getLinks() != null
                    ? objectMapper.readValue(notice.getLinks(), new TypeReference<List<LinkDto>>() {})
                    : List.of();

                List<String> imageLinksList = notice.getImageLinks() != null
                    ? objectMapper.readValue(notice.getImageLinks(), new TypeReference<List<String>>() {})
                    : List.of();

                return NoticeInfo.builder()
                    .id(notice.getId())
                    .title(notice.getTitle())
                    .content(notice.getContent())
                    .links(linksList)
                    .imageLinks(imageLinksList)
                    .createAt(notice.getCreatedAt())
                    .modifiedAt(notice.getModifiedAt())
                    .build();
            } catch (JsonProcessingException e) {
                throw new BbangleException(BbangleErrorCode.JSON_SERIALIZATION_ERROR);
            }
        }

    }



}
