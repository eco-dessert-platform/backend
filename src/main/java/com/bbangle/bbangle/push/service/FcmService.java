package com.bbangle.bbangle.push.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.dto.FcmRequest;
import com.bbangle.bbangle.push.dto.Message;
import com.bbangle.bbangle.push.repository.PushRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FcmService {

    @Value("${fcm.api-url}")
    private String FCM_API_URL;
    @Value("${fcm.key-path}")
    private String FCM_SECRET_KEY_PATH;
    @Value("${fcm.google-authentication-url}")
    private String GOOGLE_AUTHENTICATION_URL;

    private final ObjectMapper objectMapper;
    private final PushRepository pushRepository;


    public void send(List<FcmRequest> requestList) {
        OkHttpClient client = createOkHttpClient();

        for (FcmRequest request : requestList) {
            String message = makeMessage(request);
            RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));
            Request httpRequest = createHttpRequest(requestBody);

            try {
                Response httpResponse = client.newCall(httpRequest).execute();

                if (httpResponse.isSuccessful()) {
                    Push push = pushRepository.findById(request.getPushId())
                            .orElseThrow(() -> new BbangleException(BbangleErrorCode.PUSH_NOT_FOUND));

                    // 재입고 푸시 알림이 나간 경우 해당 푸시 알림을 비활성화 상태로 돌린다.
                    if (request.getPushCategory().equals(PushCategory.RESTOCK.getDescription())) {
                        push.updateActive(false);
                    }
                }
            } catch (IOException e) {
                throw new BbangleException(BbangleErrorCode.FCM_CONNECTION_ERROR);
            }
        }
    }


    private String makeMessage(FcmRequest request) {
        Message message = Message.of(request);

        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new BbangleException(BbangleErrorCode.JSON_SERIALIZATION_ERROR);
        }
    }


    private OkHttpClient createOkHttpClient() {
        return new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.SECONDS)
                .build();
    }


    private Request createHttpRequest(RequestBody requestBody) {
        return new Request.Builder()
                .url(FCM_API_URL)
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();
    }


    private String getAccessToken() {
        try (InputStream inputStream = new ClassPathResource(FCM_SECRET_KEY_PATH).getInputStream()) {
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
