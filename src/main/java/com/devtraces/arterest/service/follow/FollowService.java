package com.devtraces.arterest.service.follow;

import com.devtraces.arterest.common.constant.CommonConstant;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.follow.dto.response.FollowResponse;
import com.devtraces.arterest.model.follow.Follow;
import com.devtraces.arterest.model.follow.FollowRepository;
import com.devtraces.arterest.model.followcache.FollowRecommendationCacheRepository;
import com.devtraces.arterest.model.recommendation.FollowRecommendation;
import com.devtraces.arterest.model.recommendation.FollowRecommendationRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.devtraces.arterest.service.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowRecommendationCacheRepository followRecommendationCacheRepository;
    private final FollowRecommendationRepository followRecommendationRepository;
    private final NoticeService noticeService;

    @Async
    @Transactional
    public void createFollowRelation(Long userId, String nickname) {
        User followerUser = findUserById(userId);

        if(
            followerUser.getFollowList() != null
                && followerUser.getFollowList().size() >= CommonConstant.FOLLOW_COUNT_LIMIT
        ){
            throw BaseException.FOLLOW_LIMIT_EXCEED;
        }

        User followingUser = findUserByNickname(nickname);

        if(Objects.equals(followingUser.getId(), userId)){
            throw BaseException.FOLLOWING_SELF_NOT_ALLOWED;
        }

        if(!followRepository.existsByUserIdAndFollowingId(userId, followingUser.getId())){
            // 중복 팔로우가 아닌 경우에만 저장이 진행됨.
            // 예외를 던지지 않게 함.
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
        noticeService.createFollowNotice(followingUser.getNickname(), followerUser.getId());
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
        User targetUser = findUserByNickname(nickname);
        List<Long> followingUserIdListOfTargetUser = targetUser.getFollowList()
            .stream().map(Follow::getFollowingId).collect(Collectors.toList());

        // 그 사람들 중에서 내가 팔로우를 하고 있는지 그렇지 않은지를 일일이 판단해야 한다.
        // 따라서 내가 팔로우 하고 있는 사람들도 봐야 한다.
        Set<Long> followingUserIdSetOfRequestUser = followRepository.findAllByUserId(userId).stream()
            .map(Follow::getFollowingId).collect(Collectors.toSet());

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
        User targetUser = findUserByNickname(nickname);

        // 현재 '내'가 팔로우 하고 있는 사람들을 확인한다.
        Set<Long> followingUserIdSetOfRequestUser = followRepository.findAllByUserId(userId).stream()
            .map(Follow::getFollowingId).collect(Collectors.toSet());

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
        User unfollowTargetUser = findUserByNickname(nickname);
        followRepository.deleteByUserIdAndFollowingId(userId, unfollowTargetUser.getId());

        // 팔로우 취소되는 유저, 팔로워 유저
        noticeService.deleteNoticeWhenFollowingCanceled(unfollowTargetUser.getId(), userId);
    }

    public List<FollowResponse> getRecommendationList(Long userId) {
        List<Long> recommendedUserIdList;
        // 캐시 서버를 본다.
        recommendedUserIdList = followRecommendationCacheRepository.getFollowTargetUserIdList();
        if(recommendedUserIdList == null){
            // 없으면 DB를 본다.
            Optional<FollowRecommendation> optionalFollowRecommendation
                = followRecommendationRepository.findTopByOrderByIdDesc();
            if(optionalFollowRecommendation.isPresent()){
                recommendedUserIdList = Arrays.stream(
                    optionalFollowRecommendation.get().getFollowRecommendationTargetUsers()
                        .split(",")
                ).map(Long::parseLong).collect(Collectors.toList());
            } else {
                // DB 마저도 없으면 빈리스트 반환.
                return Collections.emptyList();
            }
        }

        // 요청을 한 유저가 팔로우 하고 있는 유저들의 주키아이디 값 set을 구한다.
        Set<Long> followingIdSetOfRequestUser = followRepository.findAllByUserId(userId).stream()
            .map(Follow::getFollowingId).collect(Collectors.toSet());

        // 리스트를 랜덤으로 섞은 후 상위 10개(recommendedUserIdList의 길이가 10 미만이면 그 만큼)를 뽑아내되,
        // 이미 팔로우 하고 있는 유저들의 id 값 set에 포함되지 않은 것만을 뽑아낸다.
        Collections.shuffle(recommendedUserIdList);
        List<Long> resultIdList = new ArrayList<>();
        for(Long id : recommendedUserIdList){
            if(
                resultIdList.size() != CommonConstant.FOLLOW_RECOMMENDATION_USER_NUMBER &&
                    !followingIdSetOfRequestUser.contains(id)
            ){
                resultIdList.add(id);
            } else break;
        }

        return userRepository.findAllByIdIn(resultIdList).stream().map(
            user -> FollowResponse.from(user, null)
        ).collect(Collectors.toList());
    }

    private User findUserById(Long userId){
        return userRepository.findById(userId).orElseThrow(
            () -> BaseException.USER_NOT_FOUND
        );
    }

    private User findUserByNickname(String nickname){
        return userRepository.findByNickname(nickname).orElseThrow(
            () -> BaseException.USER_NOT_FOUND
        );
    }
}
