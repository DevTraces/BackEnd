package com.devtraces.arterest.common.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	/* 400 */
	VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "입력값 유효성 검사에 실패하였습니다."),
	WRONG_EMAIL_OR_PASSWORD(HttpStatus.BAD_REQUEST, "WRONG_EMAIL_OR_PASSWORD", "잘못된 이메일 혹은 비밀번호입니다."),
	INVALID_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
	NOT_EXPIRED_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "NOT_EXPIRED_ACCESS_TOKEN", "만료되지 않은 Access Token입니다. 비정상적인 시도이므로 로그아웃됩니다."),

	/* 403 */
	FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근할 수 있는 권한이 없습니다."),
	EXPIRED_OR_PREVIOUS_REFRESH_TOKEN(HttpStatus.FORBIDDEN, "EXPIRED_OR_PREVIOUS_REFRESH_TOKEN", "만료되었거나 이전에 발급된 Refresh Token입니다. 새로 로그인해주세요."),

	/* 404 */
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "존재하지 않는 유저입니다."),

	/* 500 */
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "예상치 못한 서버 에러가 발생했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
