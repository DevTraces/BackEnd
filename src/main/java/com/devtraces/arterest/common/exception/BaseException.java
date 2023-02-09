package com.devtraces.arterest.common.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class BaseException extends RuntimeException {

	public static final BaseException FEED_NOT_FOUND = new BaseException(ErrorCode.FEED_NOT_FOUND);
	public static final BaseException REPLY_NOT_FOUND = new BaseException(ErrorCode.REPLY_NOT_FOUND);
	public static final BaseException REREPLY_NOT_FOUND = new BaseException(ErrorCode.REREPLY_NOT_FOUND);
	public static final BaseException USER_NOT_FOUND = new BaseException(ErrorCode.USER_NOT_FOUND);
	public static final BaseException CONTENT_LIMIT_EXCEED = new BaseException(ErrorCode.CONTENT_LIMIT_EXCEED);
	public static final BaseException USER_INFO_NOT_MATCH = new BaseException(ErrorCode.USER_INFO_NOT_MATCH);

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
