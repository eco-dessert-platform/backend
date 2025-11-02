package com.bbangle.bbangle.notification.customer.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.notification.customer.service.NotificationService;
import com.bbangle.bbangle.notification.domain.Notice;
import com.bbangle.bbangle.notification.repository.NotificationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

class NotificationControllerTest extends AbstractIntegrationTest {

    @Autowired
    NotificationService notificationService;
    @Autowired
    ResponseService responseService;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    MockMvc mockMvc;

    private Long firstSavedId;

    @BeforeEach
    void setUp() {
        createNotification();
    }

    private void createNotification() {
        LocalTime now = LocalTime.now();
        for (int i = 1; i < 24; i++) {
            LocalDate localDate = LocalDate.of(2024, 4, i);
            LocalDateTime testLocalDateTime = LocalDateTime.of(localDate, now);
            Notice notification = Notice.builder()
                .title("test" + i)
                .content("content" + i)
                .createdAt(testLocalDateTime)
                .build();
            Notice saveNotice = notificationRepository.save(notification);

            if (i == 1) {
                firstSavedId = saveNotice.getId();
            }
        }
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 : 다음 페이지가 있는 경우 hasNext는 true")
    @WithMockUser
    void hasNextPage() throws Exception {
        mockMvc.perform(get("/api/v1/notification"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.hasNext").value(true))
            .andDo(print());
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 : 다음 페이지가 없는 경우 hasNext는 false")
    @WithMockUser
    void notHasNextPage() throws Exception {
        mockMvc.perform(get("/api/v1/notification")
                .param("cursorId", String.valueOf(firstSavedId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.hasNext").value(false))
            .andDo(print());
    }
}
