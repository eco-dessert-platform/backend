package com.bbangle.bbangle.push.service;

import com.bbangle.bbangle.push.dto.PushMessage;
import com.bbangle.bbangle.push.dto.SendPushRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;
import org.apache.http.HttpHeaders;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmService {

    private final String API_URL = "https://fcm.googleapis.com/v1/projects/bbanggrioven/messages:send";
    private final ObjectMapper objectMapper;


    public void sendPush(SendPushRequest request) {
        String message = makeMessage(request);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));
        Request httpRequest = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();
        System.out.println("httpRequest = " + httpRequest);

        Response httpResponse;
        try {
            httpResponse = client.newCall(httpRequest).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("httpResponse = " + httpResponse);
    }


    private String makeMessage(SendPushRequest pushRequest) {
        PushMessage fcmMessage = PushMessage.builder()
                .notification(PushMessage.Notification.builder()
                        .token(pushRequest.token())
                        .message(PushMessage.Message.builder()
                                .title(pushRequest.title())
                                .body(pushRequest.body())
                                .image(null)
                                .build())
                        .build())
                .validateOnly(false)
                .build();

        try {
            return objectMapper.writeValueAsString(fcmMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private String getAccessToken() {
        String firebaseKeyPath = "firebase/firebase_service_key.json";
        GoogleCredentials googleCredentials;

        try {
            googleCredentials = GoogleCredentials
                    .fromStream(new ClassPathResource(firebaseKeyPath).getInputStream())
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

            googleCredentials.refreshIfExpired();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return googleCredentials.getAccessToken().getTokenValue();
    }

}
