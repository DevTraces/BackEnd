package com.devtraces.arterest.service.follow;

import com.devtraces.arterest.common.constant.CommonConstant;
import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.follow.dto.response.FollowResponse;
import com.devtraces.arterest.model.follow.Follow;
import com.devtraces.arterest.model.follow.FollowRepository;
import com.devtraces.arterest.model.followcache.FollowRecommendationCacheRepository;
import com.devtraces.arterest.model.followcache.FollowSamplePoolCacheRepository;
import com.devtraces.arterest.model.recommendation.FollowRecommendation;
import com.devtraces.arterest.model.recommendation.FollowRecommendationRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowSamplePoolCacheRepository followSamplePoolCacheRepository;
    private final FollowRecommendationCacheRepository followRecommendationCacheRepository;
    private final FollowRecommendationRepository followRecommendationRepository;

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
        User requestedUser = findUserById(userId);
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
        User targetUser = findUserByNickname(nickname);

        // 현재 '내'가 팔로우 하고 있는 사람들을 확인한다.
        User requestedUser = findUserById(userId);
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
        User unfollowTargetUser = findUserByNickname(nickname);
        followRepository.deleteByUserIdAndFollowingId(userId, unfollowTargetUser.getId());
    }

    // 팔로우 테이블의 가장 마지막 레코드(== 가장 최근 팔로우)를 찾아낸 후 캐시해 둠.
    // 매 6초마다 가장 최신 팔로우 정보를 캐시해두므로, 1시간이면 팔로우 추천 유저 후보 결정을 위한
    // 600 개의 샘플을 레디스 리스트에 저장해 둘 수 있다.
    @Scheduled(cron = CommonConstant.PUSH_SAMPLE_TO_REDIS_CRON_STRING)
    public void pushFollowSampleToCacheServer(){
        Optional<Follow> optionalLatestFollow = followRepository.findTopByOrderByIdDesc();
        optionalLatestFollow
            .ifPresent(
                follow -> followSamplePoolCacheRepository.pushSample(follow.getFollowingId())
            );
    }

    // 매 정각마다 followSamplePoolCacheRepository를 통해 레디스에 저장된
    // 팔로우 추천 대상 유저 선별용 샘플 리스트의 내용을 바탕으로
    // 최근 1시간 이내에 팔로우를 많이 받은 상위 일정 수 만큼의 유저들의 주키 아이디 값 리스트를 캐시해 둔다.
    // 캐시서버가 다운되었을 경우를 대비하여 DB에도 저장해 둔다.
    @Scheduled(cron = CommonConstant.INITIALIZE_RECOMMENDATION_LIST_TO_REDIS_CONE_STRING)
    public void initializeFollowRecommendationTargetUserIdListToCacheServer(){
        List<Long> sampleList = followSamplePoolCacheRepository.getSampleList();
        if(sampleList != null){
            Map<Long, Integer> userIdToCountMap = new HashMap<>();
            for(long id : sampleList){
                userIdToCountMap.put(id, userIdToCountMap.getOrDefault(id, 0)+1);
            }

            PriorityQueue<Map.Entry<Long, Integer>> priorityQueue = new PriorityQueue<>(
                (x,y) -> (y.getValue() - x.getValue())
            );
            for(Map.Entry<Long, Integer> entry : userIdToCountMap.entrySet()){
                priorityQueue.offer(entry);
            }

            List<Long> recommendationList = new ArrayList<>();
            for(int i=1; i<= CommonConstant.FOLLOW_RECOMMENDATION_LIST_SIZE; i++){
                if(!priorityQueue.isEmpty()){
                    recommendationList.add(priorityQueue.poll().getKey());
                } else break;
            }

            followRecommendationCacheRepository.updateRecommendationTargetUserIdList(recommendationList);

            followRecommendationRepository.save(
                FollowRecommendation.builder()
                    .followRecommendationTargetUsers(recommendationList.toString())
                    .build()
            );
        }
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
