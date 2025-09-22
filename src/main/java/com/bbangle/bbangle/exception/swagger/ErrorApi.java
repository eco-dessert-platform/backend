package com.bbangle.bbangle.exception.swagger;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.exception.BbangleException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

public interface ErrorApi {

    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class)
            )
        )
    })
    CommonResult defaultExceptionHandler(HttpServletRequest request, Exception ex);

    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "404",
            description = "Not Found Error",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class)
            )
        )
    })
    CommonResult notFoundExceptionHandler(NoResourceFoundException ex);

    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "4xx",
            description = "비즈니스 Error",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class)
            )
        )
    })
    ResponseEntity<CommonResult> handleBbangleException(HttpServletRequest request, BbangleException ex);

    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request Error",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class)
            )
        )
    })
    ResponseEntity<CommonResult> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex);

    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class)
            )
        )
    })
    ResponseEntity<CommonResult> amazonS3Exception(AmazonS3Exception e);

    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class)
            )
        )
    })
    ResponseEntity<CommonResult> sdkClientException(SdkClientException e);

}
