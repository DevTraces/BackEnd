package com.devtraces.arterest.service.follow;

import com.devtraces.arterest.common.CommonUtils;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.domain.follow.Follow;
import com.devtraces.arterest.domain.follow.FollowRepository;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createFollowRelation(Long userId, String nickname) {
        User followerUser = userRepository.findById(userId).orElseThrow(
            () -> BaseException.USER_NOT_FOUND
        );

        if(followerUser.getFollowList().size() >= CommonUtils.FOLLOW_COUNT_LIMIT){
            throw BaseException.FOLLOW_LIMIT_EXCEED;
        }

        User followingUser = userRepository.findByNickname(nickname).orElseThrow(
            () -> BaseException.USER_NOT_FOUND
        );

        // 유저 엔티티와 팔로우 엔티티는 1:N 관계이므로,
        // 유저 엔티티에는 [그 유저가 팔로우한 다른 유저의 주키 아이디 값을 저장하고 있는 팔로우 엔티티] 리스트가 저장됨.
        // 따라서 특정 유저를 찾아내면 그 유저가 팔로우 하고 있는 다른 유저들의 주키 아이디 값도 얻을 수 있음.
        followRepository.save(
            Follow.builder()
                .user(followerUser)
                .followingId(followingUser.getId())
                .build()
        );
    }

    @Transactional
    public void deleteFollowRelation(Long userId, String nickname) {
        User followingUser = userRepository.findByNickname(nickname).orElseThrow(
            () -> BaseException.USER_NOT_FOUND
        );
        followRepository.deleteByUserIdAndFollowingId(userId, followingUser.getId());
    }

}
