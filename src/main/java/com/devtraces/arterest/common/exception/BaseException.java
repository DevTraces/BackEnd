package com.devtraces.arterest.common.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class BaseException extends RuntimeException {

	public static final BaseException FAILED_FILE_UPLOAD = new BaseException(ErrorCode.FAILED_FILE_UPLOAD);
	public static final BaseException INVALID_IMAGE_EXTENSION = new BaseException(ErrorCode.INVALID_IMAGE_EXTENSION);

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
