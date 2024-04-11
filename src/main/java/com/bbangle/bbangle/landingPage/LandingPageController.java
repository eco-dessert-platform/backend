package com.bbangle.bbangle.landingPage;

import com.bbangle.bbangle.common.message.MessageResDto;
import com.bbangle.bbangle.member.dto.RequestEmailDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@RestController
@Slf4j
@RequestMapping("/api/v1")
public class LandingPageController {
    private static final String DIRECTORY_PATH = "/etc/bbangle";
    static final String FILE_NAME = "landingPageUserEmail.txt";
    @PostMapping("/landingpage")
    public ResponseEntity<MessageResDto> getUserEmail(@Valid @RequestBody RequestEmailDto requestEmailDto){
        String email = requestEmailDto.getEmail()+",";
        Path filePath = Paths.get(DIRECTORY_PATH, FILE_NAME);
        try {
            Files.createDirectories(filePath.getParent());
            if (Files.notExists(filePath)) {
                Files.createFile(filePath);
            }
            Files.write(filePath, email.getBytes(), StandardOpenOption.APPEND);
            return ResponseEntity.ok(new MessageResDto("success"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResDto(e.getMessage()));
        }
    }
}
