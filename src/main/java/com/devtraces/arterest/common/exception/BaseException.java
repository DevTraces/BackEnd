package com.devtraces.arterest.common.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class BaseException extends RuntimeException {

	public static final BaseException WRONG_EMAIL_OR_PASSWORD = new BaseException(ErrorCode.WRONG_EMAIL_OR_PASSWORD);
	public static final BaseException FORBIDDEN = new BaseException(ErrorCode.FORBIDDEN);
	public static final BaseException USER_NOT_FOUND = new BaseException(ErrorCode.USER_NOT_FOUND);

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
