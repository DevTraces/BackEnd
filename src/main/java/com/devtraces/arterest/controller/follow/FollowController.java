package com.devtraces.arterest.controller.follow;

import com.devtraces.arterest.service.follow.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

}
