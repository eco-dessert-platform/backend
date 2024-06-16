package com.bbangle.bbangle.landingPage;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.member.dto.RequestEmailDto;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LandingPageController {

    private static final String DIRECTORY_PATH = "/etc/bbangle";
    private static final String FILE_NAME = "landingPageUserEmail.txt";
    private final ResponseService responseService;

    @PostMapping("/landingpage")
    public CommonResult getUserEmail(@Valid @RequestBody RequestEmailDto requestEmailDto) {
        String email = requestEmailDto.getEmail() + ",";
        Path filePath = Paths.get(DIRECTORY_PATH, FILE_NAME);
        try {
            Files.createDirectories(filePath.getParent());
            if (Files.notExists(filePath)) {
                Files.createFile(filePath);
            }
            Files.write(filePath, email.getBytes(), StandardOpenOption.APPEND);
            return responseService.getSuccessResult();
        } catch (IOException e) {
            return responseService.getFailResult(e.getMessage(), -1);
        }
    }
}
