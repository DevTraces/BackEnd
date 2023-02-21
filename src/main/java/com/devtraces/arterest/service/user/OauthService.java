package com.devtraces.arterest.service.user;

import com.devtraces.arterest.common.type.UserSignUpType;
import com.devtraces.arterest.common.type.UserStatusType;
import com.devtraces.arterest.common.jwt.JwtProvider;
import com.devtraces.arterest.common.jwt.dto.TokenDto;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import com.devtraces.arterest.service.user.dto.UserInfoFromKakaoDto;
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

    public TokenDto oauthKakaoSignIn(String accessTokenFromKakao) {
        // kakao 서버에 액세스 토큰 보낸 뒤 사용자 정보 가져오기
        UserInfoFromKakaoDto userInfoFromKakaoDto = createKakaoUser(accessTokenFromKakao);

        // DB에 dto에서 kakaoUserId가 없는 사람만 회원가입 진행
        long kakaoUserId = userInfoFromKakaoDto.getKakaoUserId();
        Optional<User> optionalUser = userRepository.findByKakaoUserId(kakaoUserId);
        if (!optionalUser.isPresent()) {
            User save = userRepository.save(User.builder()
                    .kakaoUserId(userInfoFromKakaoDto.getKakaoUserId())
                    .email(userInfoFromKakaoDto.getEmail())
                    .username(userInfoFromKakaoDto.getUsername())
                    .nickname(userInfoFromKakaoDto.getNickname())
                    .profileImageUrl(userInfoFromKakaoDto.getProfileImageUrl())
                    .description(userInfoFromKakaoDto.getDescription())
                    .userStatus(UserStatusType.ACTIVE)
                    .signupType(UserSignUpType.KAKAO_TALK)
                    .build());

            return jwtProvider.generateAccessTokenAndRefreshToken(save.getId());
        }

        // 이미 회원가입한 사용자는 액세스, 리프레쉬 토큰 생성해서 응답
        User user = optionalUser.get();

        return jwtProvider.generateAccessTokenAndRefreshToken(user.getId());
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
                .username("사용자 이름")
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .description("나에 대한 설명을 추가해주세요!")
                .build();
    }
}
