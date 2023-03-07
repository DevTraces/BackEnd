package com.devtraces.arterest.service.auth;

import static com.devtraces.arterest.common.constant.CommonConstant.ARTIST_NAME_LIST;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.common.jwt.JwtProvider;
import com.devtraces.arterest.common.type.UserSignUpType;
import com.devtraces.arterest.common.type.UserStatusType;
import com.devtraces.arterest.controller.auth.dto.TokenWithNicknameDto;
import com.devtraces.arterest.controller.auth.dto.UserInfoFromKakaoDto;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OauthService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TokenWithNicknameDto oauthKakaoSignIn(String accessTokenFromKakao) {
        // kakao 서버에 액세스 토큰 보낸 뒤 사용자 정보 가져오기
        UserInfoFromKakaoDto userInfoFromKakaoDto =
                getUserInfoFromKakao(accessTokenFromKakao);

        assert userInfoFromKakaoDto != null;
        return kakaoSignUpOrSignIn(userInfoFromKakaoDto);
    }

    public TokenWithNicknameDto kakaoSignUpOrSignIn(UserInfoFromKakaoDto userInfoFromKakaoDto) {

        Optional<User> optionalUser =
                userRepository.findByKakaoUserId(userInfoFromKakaoDto.getKakaoUserId());

        // 카카오로 회원가입하지 않은 유저(이메일 유저 포함)는 회원가입 진행
        if (!optionalUser.isPresent()) {
            User savedUser = getSavedUser(userInfoFromKakaoDto);

            return createTokenWithNicknameDto(savedUser);
        }

        // 카카오로 회원가입한 유저는 바로 로그인 진행
        return createTokenWithNicknameDto(optionalUser.get());
    }

    // 카카오 서버로 요청하는 함수
    private UserInfoFromKakaoDto getUserInfoFromKakao(String accessTokenFromKakao) {

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
            String line;
            StringBuilder result = new StringBuilder();

            while ((line = br.readLine()) != null) {
                result.append(line);
            }

            // 카카오 서버로부터 받은 사용자 정보를 담은 DTO
            UserInfoFromKakaoDto userInfoFromKakaoDto = parseResponseToJson(result.toString());

            br.close();

            return userInfoFromKakaoDto;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 카카오 서버로부터 받은 정보 파싱
    private UserInfoFromKakaoDto parseResponseToJson(String result
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
                .username(nickname) // 카카오에서 nickname을 username으로 사용하기로 함
                .nickname(generateRandomNickname())
                .profileImageUrl(profileImageUrl)
                .description("나에 대한 설명을 추가해주세요!")
                .build();
    }

    private String generateRandomNickname() {
        String randomNickname;
        do {
            randomNickname = ARTIST_NAME_LIST.get((int) (Math.random() * 10)) + "_"
                + (int) (Math.random() * 10000000);
        } while (userRepository.existsByNickname(randomNickname));

        return randomNickname;
    }

    private User getSavedUser(UserInfoFromKakaoDto userInfoFromKakaoDto) {
        return userRepository.save(User.builder()
                .kakaoUserId(userInfoFromKakaoDto.getKakaoUserId())
                .email(userInfoFromKakaoDto.getEmail())
                // 사용자가 비밀번호에 접근하지 못하고, 소셜 로그인에서 비밀번호는 의미가 없으므로
                // 임의로 생성한 닉네임을 비밀번호로 설정
                .password(passwordEncoder.encode(userInfoFromKakaoDto.getNickname()))
                .username(userInfoFromKakaoDto.getUsername())
                .nickname(userInfoFromKakaoDto.getNickname())
                .profileImageUrl(userInfoFromKakaoDto.getProfileImageUrl())
                .description(userInfoFromKakaoDto.getDescription())
                .userStatus(UserStatusType.ACTIVE)
                .signupType(UserSignUpType.KAKAO_TALK)
                .build());
    }

    private TokenWithNicknameDto createTokenWithNicknameDto(User user) {
        return TokenWithNicknameDto.from(
                user.getNickname(),
                jwtProvider.generateAccessTokenAndRefreshToken(user.getId())
        );
    }

    private static void alreadySignUpUser(Optional<User> optionalUser) {
        if (optionalUser.isPresent() &&
                optionalUser.get().getSignupType().equals(UserSignUpType.EMAIL)
        ) {
            throw BaseException.ALREADY_EXIST_USER;
        }
    }
}
