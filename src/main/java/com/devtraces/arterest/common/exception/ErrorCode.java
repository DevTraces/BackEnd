package com.devtraces.arterest.common.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	
	/* 400 */
	VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "입력값 유효성 검사에 실패하였습니다."),
	ALREADY_EXIST_EMAIL(HttpStatus.BAD_REQUEST, "ALREADY_EXIST_EMAIL", "이미 가입된 이메일입니다."),
	WRONG_EMAIL_OR_PASSWORD(HttpStatus.BAD_REQUEST, "WRONG_EMAIL_OR_PASSWORD", "잘못된 이메일 혹은 비밀번호입니다."),
	INVALID_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
	NOT_EXPIRED_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "NOT_EXPIRED_ACCESS_TOKEN", "만료되지 않은 Access Token입니다. 비정상적인 시도이므로 로그아웃됩니다."),
 	INVALID_IMAGE_EXTENSION(HttpStatus.BAD_REQUEST, "INVALID_IMAGE_EXTENSION", "처리할 수 없는 이미지 형식입니다."),
 	CONTENT_LIMIT_EXCEED(HttpStatus.BAD_REQUEST,"CONTENT_LIMIT_EXCEED","1000자를 초과하여 입력할 수 없습니다."),
 	USER_INFO_NOT_MATCH(HttpStatus.BAD_REQUEST,"USER_INFO_NOT_MATCH","작성자만 내용을 수정할 수 있습니다."),
	HASHTAG_LIMIT_EXCEED(HttpStatus.BAD_REQUEST, "HASHTAG_LIMIT_EXCEED", "해시태그는 10개를 초과하여 입력할 수 없습니다."),
	IMAGE_FILE_COUNT_LIMIT_EXCEED(HttpStatus.BAD_REQUEST, "IMAGE_FILE_COUNT_LIMIT_EXCEED", "이미지 파일의 개수는 15개를 초과할 수 없습니다."),
	NULL_AND_EMPTY_STRING_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "NULL_AND_EMPTY_STRING_NOT_ALLOWED", "빈 문자열 입력은 불가능합니다."),
  
	/* 401 */
	ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "ACCESS_DENIED", "유효한 인증 정보가 부족합니다."),
	EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_ACCESS_TOKEN", "Access Token이 만료되었습니다. 토큰을 재발급해주세요"),

	/* 403 */
	FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근할 수 있는 권한이 없습니다."),
	EXPIRED_OR_PREVIOUS_REFRESH_TOKEN(HttpStatus.FORBIDDEN, "EXPIRED_OR_PREVIOUS_REFRESH_TOKEN", "만료되었거나 이전에 발급된 Refresh Token입니다. 새로 로그인해주세요."),

	/* 404 */
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "존재하지 않는 유저입니다."),
 	FEED_NOT_FOUND(HttpStatus.NOT_FOUND, "FEED_NOT_FOUND", "존재하지 않는 게시물입니다."),
 	REPLY_NOT_FOUND(HttpStatus.BAD_REQUEST, "REPLY_NOT_FOUND", "존재하지 않는 댓글입니다."),
 	REREPLY_NOT_FOUND(HttpStatus.BAD_REQUEST, "REREPLY_NOT_FOUND", "존재하지 않는 대댓글입니다."),

	/* 500 */
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "예상치 못한 서버 에러가 발생했습니다."),
 	FAILED_FILE_UPLOAD(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED_FILE_UPLOAD", "파일 업로드에 실패했습니다."),
	FAILED_CACHE_GET_OPERATION(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED_CACHE_GET_OPERATION", "캐시서버에서 값을 가져오지 못했습니다."),
	FAILED_CACHE_PUT_OPERATION(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED_CACHE_PUT_OPERATION", "캐시서버에 값을 등록하는 것에 실패했습니다."),
	FAILED_SEND_EMAIL(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED_SEND_EMAIL", "메일 전송에 실패했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
