package com.bbangle.bbangle.token.controller;

import com.bbangle.bbangle.token.dto.CreateAccessTokenRequest;
import com.bbangle.bbangle.token.dto.CreateAccessTokenResponse;
import com.bbangle.bbangle.token.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TokenApiController {

    private final TokenService tokenService;

    @PostMapping("/api/v1/token")
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(
        @RequestBody
        CreateAccessTokenRequest request
    ) {
        String newAccessToken = tokenService.createNewAccessToken(request.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CreateAccessTokenResponse(newAccessToken));
    }

}
