package com.bbangle.bbangle.push.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.push.dto.FcmRequest;
import com.bbangle.bbangle.push.dto.FcmMessageDto;
import com.bbangle.bbangle.push.repository.PushRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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
import java.util.concurrent.TimeUnit;

//TO.순원님
//현재 주석 처리하거나 사용되지 않는 코드들은 다 이전 코드라고 생각해주시면 되요
//나중에 쓸 일이 있을 수도 있을 꺼 같아서 남겨놨고 여기는 건드리실 필요 없습니다!
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

    /*public void send(List<FcmRequest> requestList) {
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
    }*/
    //FIXME FOR TEST
    /*public void send(Message msg) {
        OkHttpClient client = createOkHttpClient();

            String message = makeMessage(msg);
            RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));
            Request httpRequest = createHttpRequest(requestBody);
            try {
                client.newCall(httpRequest).execute();
            } catch (IOException e) {
                throw new BbangleException(BbangleErrorCode.FCM_CONNECTION_ERROR);
            }
    }*/

    public void sendMessageTo(FcmRequest fcmRequest) throws IOException {
        String message = makeMessage(fcmRequest);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer "+ getAccessToken());

        HttpEntity<String> entity = new HttpEntity<>(message, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
                FCM_API_URL,
                HttpMethod.POST,
                entity,
                String.class);

    }


    private String makeMessage(FcmRequest request) {
        FcmMessageDto fcmMessageDto = FcmMessageDto.of(request);

        try {
            return objectMapper.writeValueAsString(fcmMessageDto);
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
