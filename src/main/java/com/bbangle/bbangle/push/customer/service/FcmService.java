package com.bbangle.bbangle.push.customer.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.customer.dto.FcmRequest;
import com.bbangle.bbangle.push.customer.dto.FcmTestDto;
import com.bbangle.bbangle.push.repository.PushRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {
    private final PushRepository pushRepository;

    public void sendMessage(List<FcmRequest> fcmRequests) {
        for(FcmRequest fcmRequest : fcmRequests) {
            Push push = pushRepository.findById(fcmRequest.getPushId())
                    .orElseThrow(() -> new BbangleException(BbangleErrorCode.PUSH_NOT_FOUND));
            Message message = getMessage(fcmRequest);
            try{
                FirebaseMessaging.getInstance().send(message);
                if (fcmRequest.getPushCategory().equals(PushCategory.RESTOCK.getDescription())) {
                    push.updateActive(false);
                }
            }catch (FirebaseMessagingException e){
                log.error(e.getMessage());
            }
        }
    }

    private static Message getMessage(FcmRequest fcmRequest) {
       return Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(fcmRequest.getTitle())
                        .setBody(fcmRequest.getBody())
                        .build())
                .setToken(fcmRequest.getFcmToken())
                .build();
    }

    public void sendTest(FcmTestDto fcmTestDto){
        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(fcmTestDto.title())
                        .setBody(fcmTestDto.body())
                        .build())
                .setToken(fcmTestDto.fcmToken())
                .build();
        try{
            FirebaseMessaging.getInstance().send(message);
        }catch (FirebaseMessagingException e){
            log.error(e.getMessage());
        }
    }
}
