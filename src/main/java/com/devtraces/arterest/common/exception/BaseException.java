package com.devtraces.arterest.common.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class BaseException extends RuntimeException {

	public static final BaseException ALREADY_EXIST_EMAIL = new BaseException(ErrorCode.ALREADY_EXIST_EMAIL);
	public static final BaseException REPLY_NOT_FOUND = new BaseException(ErrorCode.REPLY_NOT_FOUND);
	public static final BaseException REREPLY_NOT_FOUND = new BaseException(ErrorCode.REREPLY_NOT_FOUND);
	public static final BaseException CONTENT_LIMIT_EXCEED = new BaseException(ErrorCode.CONTENT_LIMIT_EXCEED);
	public static final BaseException USER_INFO_NOT_MATCH = new BaseException(ErrorCode.USER_INFO_NOT_MATCH);
	public static final BaseException WRONG_EMAIL_OR_PASSWORD = new BaseException(ErrorCode.WRONG_EMAIL_OR_PASSWORD);
	public static final BaseException NOT_EXPIRED_ACCESS_TOKEN = new BaseException(ErrorCode.NOT_EXPIRED_ACCESS_TOKEN);
  	public static final BaseException INVALID_IMAGE_EXTENSION = new BaseException(ErrorCode.INVALID_IMAGE_EXTENSION);
	public static final BaseException INVALID_TOKEN = new BaseException(ErrorCode.INVALID_TOKEN);
	public static final BaseException FORBIDDEN = new BaseException(ErrorCode.FORBIDDEN);
	public static final BaseException EXPIRED_OR_PREVIOUS_REFRESH_TOKEN = new BaseException(ErrorCode.EXPIRED_OR_PREVIOUS_REFRESH_TOKEN);
	public static final BaseException USER_NOT_FOUND = new BaseException(ErrorCode.USER_NOT_FOUND);
	public static final BaseException FEED_NOT_FOUND = new BaseException(ErrorCode.FEED_NOT_FOUND);
	public static final BaseException INTERNAL_SERVER_ERROR = new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
	public static final BaseException FAILED_FILE_UPLOAD = new BaseException(ErrorCode.FAILED_FILE_UPLOAD);
	public static final BaseException FAILED_SEND_EMAIL = new BaseException(ErrorCode.FAILED_SEND_EMAIL);

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
