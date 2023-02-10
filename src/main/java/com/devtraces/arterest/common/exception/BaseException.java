package com.devtraces.arterest.common.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class BaseException extends RuntimeException {

	public static final BaseException WRONG_EMAIL_OR_PASSWORD = new BaseException(ErrorCode.WRONG_EMAIL_OR_PASSWORD);
	public static final BaseException NOT_EXPIRED_ACCESS_TOKEN = new BaseException(ErrorCode.NOT_EXPIRED_ACCESS_TOKEN);
  public static final BaseException INVALID_IMAGE_EXTENSION = new BaseException(ErrorCode.INVALID_IMAGE_EXTENSION);
	public static final BaseException INVALID_TOKEN = new BaseException(ErrorCode.INVALID_TOKEN);
	public static final BaseException FORBIDDEN = new BaseException(ErrorCode.FORBIDDEN);
	public static final BaseException EXPIRED_OR_PREVIOUS_REFRESH_TOKEN = new BaseException(ErrorCode.EXPIRED_OR_PREVIOUS_REFRESH_TOKEN);
	public static final BaseException USER_NOT_FOUND = new BaseException(ErrorCode.USER_NOT_FOUND);
  public static final BaseException FAILED_FILE_UPLOAD = new BaseException(ErrorCode.FAILED_FILE_UPLOAD);

	private final ErrorCode errorCode;

	// 의도적인 예외이므로 stack trace 제거 (불필요한 예외처리 비용 제거)
	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	public HttpStatus getHttpStatus() {
		return errorCode.getHttpStatus();
	}
}
