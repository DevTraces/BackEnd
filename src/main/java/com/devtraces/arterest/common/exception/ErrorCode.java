package com.devtraces.arterest.common.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	/* 400 */
	VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "입력값 유효성 검사에 실패하였습니다."),
	WRONG_EMAIL_OR_PASSWORD(HttpStatus.BAD_REQUEST, "WRONG_EMAIL_OR_PASSWORD", "잘못된 이메일 혹은 비밀번호입니다."),

	/* 404 */
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "존재하지 않는 유저입니다."),

	/* 500 */
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "예상치 못한 서버 에러가 발생했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
