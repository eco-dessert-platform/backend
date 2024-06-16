package com.bbangle.bbangle.exception;

import static com.bbangle.bbangle.exception.BbangleErrorCode.AWS_ACL_BLOCK;
import static com.bbangle.bbangle.exception.BbangleErrorCode.AWS_ENVIRONMENT;
import static org.springframework.util.StringUtils.hasText;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.bbangle.bbangle.common.adaptor.slack.SlackAdaptor;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final ResponseService responseService;
    private final SlackAdaptor slackAdaptor;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CommonResult defaultExceptionHandler(HttpServletRequest request, Exception ex) {
        slackAdaptor.sendAlert(request, ex);
        return responseService.getFailResult(ex.getLocalizedMessage(), -1);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public CommonResult notFoundExceptionHandler(NoResourceFoundException ex) {
        // 404 는 쓸데없는 알람이 너무 많이와서 얼럿에서 제외
        return responseService.getFailResult(ex.getLocalizedMessage(), -1);
    }

    @ExceptionHandler(BbangleException.class)
    public ResponseEntity<CommonResult> handleBbangleException(
        HttpServletRequest request,
        BbangleException ex
    ) {
        slackAdaptor.sendAlert(request, ex);
        CommonResult result = responseService.getFailResult(
            hasText(ex.getMessage()) ? ex.getMessage() : "error",
            ex.getBbangleErrorCode().getCode()
        );
        return new ResponseEntity<>(result, ex.getBbangleErrorCode().getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResult> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException ex
    ) {
        CommonResult methodArgumentNotValidExceptionResult = responseService
            .getMethodArgumentNotValidExceptionResult(ex);
        return new ResponseEntity<>(methodArgumentNotValidExceptionResult, HttpStatus.BAD_REQUEST);
    }

    //아마존 S3 ACL 권한 설정 안했을 시 에러 발생
    @ExceptionHandler(value = AmazonS3Exception.class)
    public ResponseEntity<CommonResult> amazonS3Exception(AmazonS3Exception e) {
        log.error(String.format("%s:\n%s", e, AWS_ACL_BLOCK.getMessage()));

        return ResponseEntity.internalServerError()
            .body(responseService.getError(AWS_ACL_BLOCK));
    }

    @ExceptionHandler(value = SdkClientException.class)
    public ResponseEntity<CommonResult> sdkClientException(SdkClientException e) {
        // build.gradle에, spring-cloud-starter-aws 의존성 주입시
        // 로컬환경은, aws환경이 아니기때문에 나는 에러
        log.error(String.format("%s:\n%s", e, AWS_ENVIRONMENT));

        return ResponseEntity.internalServerError()
            .body(responseService.getError(AWS_ENVIRONMENT));
    }

}
