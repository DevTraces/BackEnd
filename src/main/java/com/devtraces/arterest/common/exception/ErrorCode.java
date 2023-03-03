package com.devtraces.arterest.common.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	
	/* 400 */
	VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "입력값 유효성 검사에 실패하였습니다."),
	NOT_AUTHENTICATION_YET(HttpStatus.BAD_REQUEST, "NOT_AUTHENTICATION_YET", "이메일 인증을 하지 않은 사용자입니다."),
	ALREADY_EXIST_EMAIL(HttpStatus.BAD_REQUEST, "ALREADY_EXIST_EMAIL", "이미 가입된 이메일입니다."),
	ALREADY_EXIST_NICKNAME(HttpStatus.BAD_REQUEST, "ALREADY_EXIST_NICKNAME", "중복된 닉네임입니다."),
	ALREADY_EXIST_USER(HttpStatus.BAD_REQUEST, "ALREADY_EXIST_USER", "이미 가입한 회원입니다." ),
	WRONG_EMAIL_OR_PASSWORD(HttpStatus.BAD_REQUEST, "WRONG_EMAIL_OR_PASSWORD", "잘못된 이메일 혹은 비밀번호입니다."),
	UPDATE_PASSWORD_NOT_ALLOWED_FOR_KAKAO_USER(HttpStatus.BAD_REQUEST, "UPDATE_PASSWORD_NOT_ALLOWED_FOR_KAKAO_USER", "카카오로 가입한 사용자는 비밀번호를 설정할 수 없습니다."),
	WRONG_BEFORE_PASSWORD(HttpStatus.BAD_REQUEST, "WRONG_BEFORE_PASSWORD", "이전 비밀번호가 일치하지 않습니다."),
	INVALID_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
	NOT_EXPIRED_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "NOT_EXPIRED_ACCESS_TOKEN", "만료되지 않은 Access Token입니다."),
	FOLLOW_LIMIT_EXCEED(HttpStatus.BAD_REQUEST, "FOLLOW_LIMIT_EXCEED", "5000명을 초과하여 팔로우 할 수 없습니다."),
	FOLLOWING_SELF_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "FOLLOWING_SELF_NOT_ALLOWED", "자기자신을 팔로우 하는 것은 불가능합니다."),
 	INVALID_IMAGE_EXTENSION(HttpStatus.BAD_REQUEST, "INVALID_IMAGE_EXTENSION", "처리할 수 없는 이미지 형식입니다."),
 	CONTENT_LIMIT_EXCEED(HttpStatus.BAD_REQUEST,"CONTENT_LIMIT_EXCEED","1000자를 초과하여 입력할 수 없습니다."),
 	USER_INFO_NOT_MATCH(HttpStatus.BAD_REQUEST,"USER_INFO_NOT_MATCH","작성자만 내용을 수정할 수 있습니다."),
	HASHTAG_LIMIT_EXCEED(HttpStatus.BAD_REQUEST, "HASHTAG_LIMIT_EXCEED", "해시태그는 10개를 초과하여 입력할 수 없습니다."),
	IMAGE_FILE_COUNT_LIMIT_EXCEED(HttpStatus.BAD_REQUEST, "IMAGE_FILE_COUNT_LIMIT_EXCEED", "이미지 파일의 개수는 15개를 초과할 수 없습니다."),
	NULL_AND_EMPTY_STRING_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "NULL_AND_EMPTY_STRING_NOT_ALLOWED", "빈 문자열 입력은 불가능합니다."),
	LIKE_NUMBER_BELLOW_ZERO(HttpStatus.BAD_REQUEST, "LIKE_NUMBER_BELLOW_ZERO", "좋아요 개수가 0 입니다."),
  
	/* 401 */
	ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "ACCESS_DENIED", "유효한 인증 정보가 아닙니다."),
	EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_ACCESS_TOKEN", "Access Token이 만료되었습니다. 토큰을 재발급해주세요"),

	/* 403 */
	FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근할 수 있는 권한이 없습니다."),
	EXPIRED_OR_PREVIOUS_REFRESH_TOKEN(HttpStatus.FORBIDDEN, "EXPIRED_OR_PREVIOUS_REFRESH_TOKEN", "만료되었거나 이전에 발급된 Refresh Token입니다."),

	/* 404 */
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "존재하지 않는 유저입니다."),
 	FEED_NOT_FOUND(HttpStatus.NOT_FOUND, "FEED_NOT_FOUND", "존재하지 않는 게시물입니다."),
 	REPLY_NOT_FOUND(HttpStatus.BAD_REQUEST, "REPLY_NOT_FOUND", "존재하지 않는 댓글입니다."),
 	REREPLY_NOT_FOUND(HttpStatus.BAD_REQUEST, "REREPLY_NOT_FOUND", "존재하지 않는 대댓글입니다."),
	NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE_NOT_FOUND" , "존재하지 않는 알림 입니다."),

	/* 500 */
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "예상치 못한 서버 에러가 발생했습니다."),
 	FAILED_FILE_UPLOAD(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED_FILE_UPLOAD", "파일 업로드에 실패했습니다."),
	FAILED_CACHE_GET_OPERATION(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED_CACHE_GET_OPERATION", "캐시서버에서 값을 가져오지 못했습니다."),
	FAILED_CACHE_PUT_OPERATION(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED_CACHE_PUT_OPERATION", "캐시서버에 값을 등록하는 것에 실패했습니다."),
	FAILED_SEND_EMAIL(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED_SEND_EMAIL", "메일 전송에 실패했습니다."),
	DUPLICATED_FOLLOW_OR_LIKE(HttpStatus.INTERNAL_SERVER_ERROR, "DUPLICATED_FOLLOW_OR_LIKE", "이미 팔로우 또는 좋아요를 하였습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
