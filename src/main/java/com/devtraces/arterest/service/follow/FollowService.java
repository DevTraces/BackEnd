package com.devtraces.arterest.service.follow;

import com.devtraces.arterest.common.CommonUtils;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.follow.dto.FollowResponse;
import com.devtraces.arterest.domain.follow.Follow;
import com.devtraces.arterest.domain.follow.FollowRepository;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import com.fasterxml.jackson.databind.ser.Serializers.Base;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

        if(Objects.equals(followingUser.getId(), userId)){
            throw BaseException.FOLLOWING_SELF_NOT_ALLOWED;
        }

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

    /*
    * nickname 이라는 닉네임을 가지고 있는 타깃 유저가 팔로우 하고 있는 다른 유저들의 리스트를
    * 반환하되, "내" 입장에서 이 유저들을 현재 팔로우를 하고 있는 지의 여부가 전부 boolean 필드로
    * 표시되어야 한다.
    * */
    @Transactional(readOnly = true)
    public List<FollowResponse> getFollowingUserList(
        Long userId, String nickname, Integer page, Integer pageSize
    ) {
        // 타깃 유저가 팔로우 하고 있는 사람들이 누구인지를 체크한다.
        // 타깃 유저 엔티티의 팔로우 엔티티 내의 followingId 필드를 보면 된다.
        User targetUser = userRepository.findByNickname(nickname).orElseThrow(
            () -> BaseException.USER_NOT_FOUND
        );
        List<Long> followingUserIdListOfTargetUser = targetUser.getFollowList()
            .stream().map(Follow::getFollowingId).collect(Collectors.toList());

        // 그 사람들 중에서 내가 팔로우를 하고 있는지 그렇지 않은지를 일일이 판단해야 한다.
        // 따라서 내가 팔로우 하고 있는 사람들도 봐야 한다.
        User requestedUser = userRepository.findById(userId).orElseThrow(
            () -> BaseException.USER_NOT_FOUND
        );
        Set<Long> followingUserIdSetOfRequestUser = requestedUser.getFollowList()
            .stream().map(Follow::getFollowingId).collect(Collectors.toSet());

        return userRepository.findAllByIdIn(
            followingUserIdListOfTargetUser, PageRequest.of(page, pageSize)
        ).getContent().stream().map(
            user -> FollowResponse.from(user, followingUserIdSetOfRequestUser)
        ).collect(Collectors.toList());
    }

    /*
    * nickname 이라는 닉네임을 갖고 있는 타깃 유저를 팔로우 하고 있는 다른 유저들의 리스트를 반환하되,
    * 그 유저들을 "내"가 현재 팔로우를 하고 있는지를 일일이 판별하고 boolean 필드로 기록하면서 응답을 구성해야 함.
    * */
    @Transactional(readOnly = true)
    public List<FollowResponse> getFollowerUserList(
        Long userId, String nickname, Integer page, Integer pageSize
    ) {
        // 타깃 유저를 찾아낸다.
        User targetUser = userRepository.findByNickname(nickname).orElseThrow(
            () -> BaseException.USER_NOT_FOUND
        );

        // 현재 '내'가 팔로우 하고 있는 사람들을 확인한다.
        User requestedUser = userRepository.findById(userId).orElseThrow(
            () -> BaseException.USER_NOT_FOUND
        );
        Set<Long> followingUserIdSetOfRequestUser = requestedUser.getFollowList()
            .stream().map(Follow::getFollowingId).collect(Collectors.toSet());

        // 팔로우 리포지토리에서 팔로우를 보낸 유저 엔티티의 정보를 얻을 수 있으므로, 팔로우 리포지토리에서 바로
        // 응답을 구성해 낼 수 있다.
        return followRepository.findAllByFollowingId(
            targetUser.getId(), PageRequest.of(page, pageSize)
        ).getContent().stream().map(
            follow -> FollowResponse.from(follow.getUser(), followingUserIdSetOfRequestUser)
            ).collect(Collectors.toList());
    }

    @Transactional
    public void deleteFollowRelation(Long userId, String nickname) {
        User followingUser = userRepository.findByNickname(nickname).orElseThrow(
            () -> BaseException.USER_NOT_FOUND
        );
        followRepository.deleteByUserIdAndFollowingId(userId, followingUser.getId());
    }
}
