package com.devtraces.arterest.common.exception;

import com.devtraces.arterest.common.response.*;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ErrorController {

	@ExceptionHandler(BaseException.class)
	protected ResponseEntity<ApiErrorResponse> handleBaseException(BaseException e) {
		return ResponseEntity
			.status(e.getHttpStatus())
			.body(ApiErrorResponse.from(e.getErrorCode()));
	}

	// @Valid에서 binding error 발생
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ApiErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		List<String> params = new ArrayList<>();
		for (FieldError error : e.getBindingResult().getFieldErrors()) {
			params.add(error.getField() + ": " + error.getDefaultMessage());
		}
		String errorMessage = String.join(", ", params);

		ApiErrorResponse response = ApiErrorResponse.from(ErrorCode.VALIDATION_FAILED);
		response.changeMessage(errorMessage);

		return response;
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(RuntimeException.class)
	protected ApiErrorResponse handleRuntimeException(RuntimeException e) {
		log.error(e.getMessage());
		return ApiErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR);
	}
}
