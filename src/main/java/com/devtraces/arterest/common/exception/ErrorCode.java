package com.devtraces.arterest.common.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "예상치 못한 서버 에러가 발생했습니다."),
	FEED_NOT_FOUND(HttpStatus.BAD_REQUEST, "FEED_NOT_FOUND", "존재하지 않는 게시물입니다."),
	REPLY_NOT_FOUND(HttpStatus.BAD_REQUEST, "REPLY_NOT_FOUND", "존재하지 않는 댓글입니다."),
	REREPLY_NOT_FOUND(HttpStatus.BAD_REQUEST, "REREPLY_NOT_FOUND", "존재하지 않는 대댓글입니다."),
	USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND", "존재하지 않는 유저입니다."),
	CONTENT_LIMIT_EXCEED(HttpStatus.BAD_REQUEST,"CONTENT_LIMIT_EXCEED","1000자를 초과하여 입력할 수 없습니다."),
	USER_INFO_NOT_MATCH(HttpStatus.BAD_REQUEST,"USER_INFO_NOT_MATCH","작성자만 내용을 수정할 수 있습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
