package com.bbangle.bbangle.exception;

import lombok.Getter;

@Getter
public class BbangleException extends RuntimeException {

  private BbangleErrorCode bbangleErrorCode = BbangleErrorCode.INTERNAL_SERVER_ERROR;

  public BbangleException() {
    super("error");
  }

  public BbangleException(String message) {
    super(message);
  }

  public BbangleException(BbangleErrorCode bbangleErrorCode) {
    super(bbangleErrorCode.getMessage());
    this.bbangleErrorCode = bbangleErrorCode;
  }


  public BbangleException(Throwable cause) {
    super(cause);
  }

  // 문자열 가급적 쓰지말자
  @Deprecated
  public BbangleException(String message, Throwable cause) {
    super(message, cause);
  }

  public BbangleException(BbangleErrorCode bbangleErrorCode, Throwable cause) {
    super(bbangleErrorCode.getMessage(), cause);
    this.bbangleErrorCode = bbangleErrorCode;
  }
}
