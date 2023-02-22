package com.devtraces.arterest.service.auth;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.common.type.UserSignUpType;
import com.devtraces.arterest.common.type.UserStatusType;
import com.devtraces.arterest.common.jwt.JwtProvider;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import com.devtraces.arterest.controller.auth.dto.TokenWithNicknameDto;
import com.devtraces.arterest.controller.auth.dto.UserInfoFromKakaoDto;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OauthService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public TokenWithNicknameDto oauthKakaoSignIn(String accessTokenFromKakao) {
        // kakao 서버에 액세스 토큰 보낸 뒤 사용자 정보 가져오기
        UserInfoFromKakaoDto userInfoFromKakaoDto = createKakaoUser(accessTokenFromKakao);

        // 중복된 닉네임이 있다면 이미 가입한 회원으로 간주함
        validateUser(userInfoFromKakaoDto);

        // 닉네임 중복이 아니고, kakaoUserId가 없는 사람만 회원가입 실행
        long kakaoUserId = userInfoFromKakaoDto.getKakaoUserId();
        Optional<User> optionalUser = userRepository.findByKakaoUserId(kakaoUserId);
        if (!optionalUser.isPresent()) {
            User savedUser = getSavedUser(userInfoFromKakaoDto, kakaoUserId);

            return createTokenWithNicknameDto(savedUser);
        }

        // 닉네임 중복 아니고, kakao로 회원가입한 유저는 로그인 실행
        return createTokenWithNicknameDto(optionalUser.get());
    }

    // 카카오 서버로 요청하는 함수
    private UserInfoFromKakaoDto createKakaoUser(String accessTokenFromKakao) {

        String requestURL = "https://kapi.kakao.com/v2/user/me";

        // accessToken을 이용하여 사용자 정보 조회
        try {
            URL url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            //전송할 header 작성, accessToken 전송
            conn.setRequestProperty("Authorization", "Bearer " + accessTokenFromKakao);

            // 요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }

            // 카카오 서버로부터 받은 사용자 정보를 담은 DTO
            UserInfoFromKakaoDto userInfoFromKakaoDto = parseResponseToJson(result);

            br.close();

            return userInfoFromKakaoDto;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 카카오 서버로부터 받은 정보 파싱
    private static UserInfoFromKakaoDto parseResponseToJson(String result
    ) throws IOException {

        // Gson 라이브러리로 JSON 파싱
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(result);

        long kakaoUserId = element.getAsJsonObject()
                .get("id").getAsLong();
        String nickname = element.getAsJsonObject()
                .get("properties").getAsJsonObject()
                .get("nickname").getAsString();
        String profileImageUrl = element.getAsJsonObject()
                .get("properties").getAsJsonObject()
                .get("profile_image").getAsString();

        String email = null;
        boolean hasEmail = element.getAsJsonObject().get("kakao_account")
                .getAsJsonObject().get("has_email").getAsBoolean();
        JsonElement jsonEmailElement = element.getAsJsonObject().get("kakao_account")
                .getAsJsonObject().get("email");
        if(hasEmail && jsonEmailElement != null) {
            email = jsonEmailElement.getAsString();
        }

        return UserInfoFromKakaoDto.builder()
                .kakaoUserId(kakaoUserId)
                .email(email)
                .username(nickname)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .description("나에 대한 설명을 추가해주세요!")
                .build();
    }

    private void validateUser(UserInfoFromKakaoDto userInfoFromKakaoDto) {
        if (userRepository.findByNickname(userInfoFromKakaoDto.getNickname()).isPresent()) {
            throw BaseException.ALREADY_EXIST_USER;
        }
    }

    private User getSavedUser(UserInfoFromKakaoDto userInfoFromKakaoDto, long kakaoUserId) {
        User savedUser = userRepository.save(User.builder()
                .kakaoUserId(kakaoUserId)
                .email(userInfoFromKakaoDto.getEmail())
                .username(userInfoFromKakaoDto.getUsername())
                .nickname(userInfoFromKakaoDto.getNickname())
                .profileImageUrl(userInfoFromKakaoDto.getProfileImageUrl())
                .description(userInfoFromKakaoDto.getDescription())
                .userStatus(UserStatusType.ACTIVE)
                .signupType(UserSignUpType.KAKAO_TALK)
                .build());
        return savedUser;
    }

    private TokenWithNicknameDto createTokenWithNicknameDto(User user) {
        return TokenWithNicknameDto.from(
                user.getNickname(),
                jwtProvider.generateAccessTokenAndRefreshToken(user.getId())
        );
    }
}
