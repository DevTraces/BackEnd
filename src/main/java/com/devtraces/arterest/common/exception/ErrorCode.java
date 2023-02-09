package com.devtraces.arterest.common.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	/* 400 */
	INVALID_IMAGE_EXTENSION(HttpStatus.BAD_REQUEST, "INVALID_IMAGE_EXTENSION", "처리할 수 없는 이미지 형식입니다."),

	/* 404 */
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "존재하지 않는 유저입니다."),

	/* 500 */
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "예상치 못한 서버 에러가 발생했습니다."),
	FAILED_FILE_UPLOAD(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED_FILE_UPLOAD", "파일 업로드에 실패했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
