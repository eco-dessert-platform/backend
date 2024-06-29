package com.bbangle.bbangle.push.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.push.dto.FcmRequest;
import com.bbangle.bbangle.push.dto.Notification;
import com.bbangle.bbangle.push.dto.Notification.Message;
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


    public void send(List<FcmRequest> requestList) {
        for (FcmRequest request : requestList) {
            String message = makeMessage(request);
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));
            Request httpRequest = new Request.Builder()
                    .url(FCM_API_URL)
                    .post(requestBody)
                    .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                    .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                    .build();
            System.out.println("httpRequest = " + httpRequest);

            Response httpResponse;
            try {
                httpResponse = client.newCall(httpRequest).execute();
            } catch (IOException e) {
                throw new BbangleException(BbangleErrorCode.FCM_CONNECTION_ERROR);
            }

            System.out.println("httpResponse = " + httpResponse);
        }
    }


    private String makeMessage(FcmRequest request) {
        Notification notification = Notification.of(
                request.getFcmToken(),
                Message.of(
                    request.getTitle(),
                    request.getBody()
                )
        );

        try {
            return objectMapper.writeValueAsString(notification);
        } catch (JsonProcessingException e) {
            throw new BbangleException(BbangleErrorCode.JSON_SERIALIZATION_ERROR);
        }
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
