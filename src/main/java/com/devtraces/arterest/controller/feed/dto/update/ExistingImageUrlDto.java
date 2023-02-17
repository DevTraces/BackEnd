package com.devtraces.arterest.controller.feed.dto.update;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExistingImageUrlDto {

    private String imageUrl;

    private Integer index;

}
