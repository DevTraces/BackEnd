package com.devtraces.arterest.common.jwt.dto;

import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReissueRequest {

	@NotBlank(message = "닉네임 입력은 필수입니다.")
	private String nickname;
}
