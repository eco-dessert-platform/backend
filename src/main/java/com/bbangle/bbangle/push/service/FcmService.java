package com.bbangle.bbangle.push.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.dto.FcmMessageDto;
import com.bbangle.bbangle.push.dto.FcmRequest;
import com.bbangle.bbangle.push.repository.PushRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmService {

    @Value("${fcm.api-url}")
    private String FCM_API_URL;
    @Value("${fcm.google-authentication-url}")
    private String GOOGLE_AUTHENTICATION_URL;

    private final ObjectMapper objectMapper;
    private final PushRepository pushRepository;

    public void sendMessage(List<FcmRequest> fcmRequests) {
        for(FcmRequest fcmRequest : fcmRequests) {
            String message = makeMessage(fcmRequest);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters()
                    .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

            HttpEntity<String> entity = getHttpEntity(message);

            try{
                ResponseEntity<String> responseEntity = restTemplate.exchange(
                        FCM_API_URL,
                        HttpMethod.POST,
                        entity,
                        String.class);
                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    Push push = pushRepository.findById(fcmRequest.getPushId())
                            .orElseThrow(() -> new BbangleException(BbangleErrorCode.PUSH_NOT_FOUND));

                    // 재입고 푸시 알림이 나간 경우 해당 푸시 알림을 비활성화 상태로 돌린다.
                    if (fcmRequest.getPushCategory().equals(PushCategory.RESTOCK.getDescription())) {
                        push.updateActive(false);
                    }
                }
            } catch (RuntimeException e) {
                throw new BbangleException(BbangleErrorCode.FCM_CONNECTION_ERROR);
            }
        }
    }

    private @NotNull HttpEntity<String> getHttpEntity(String message) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken());
        return new HttpEntity<>(message, httpHeaders);
    }


    private String makeMessage(FcmRequest request) {
        FcmMessageDto fcmMessageDto = FcmMessageDto.of(request);

        try {
            return objectMapper.writeValueAsString(fcmMessageDto);
        } catch (JsonProcessingException e) {
            throw new BbangleException(BbangleErrorCode.JSON_SERIALIZATION_ERROR);
        }
    }

    private String getAccessToken() {
        try {
            InputStream inputStream = new ClassPathResource("firebase/firebase_service_key.json").getInputStream();
            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(inputStream)
                    .createScoped(List.of(GOOGLE_AUTHENTICATION_URL));

            googleCredentials.refreshIfExpired();
            return googleCredentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            throw new BbangleException(BbangleErrorCode.GOOGLE_AUTHENTICATION_ERROR);
        }
    }

}
