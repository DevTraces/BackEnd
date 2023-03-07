package com.devtraces.arterest.model.user;

import com.devtraces.arterest.common.type.UserSignUpType;
import com.devtraces.arterest.common.type.UserStatusType;
import com.devtraces.arterest.common.model.BaseEntity;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.follow.Follow;
import com.devtraces.arterest.model.reply.Reply;
import com.devtraces.arterest.model.rereply.Rereply;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.envers.AuditOverride;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AuditOverride(forClass = BaseEntity.class)
@Table(name = "user", indexes = {
    @Index(name = "username_index", columnList = "username"),
    @Index(name = "nickname_index", columnList = "nickname")
})
@Entity
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private Long kakaoUserId;
    @Column(name = "username", length = 100)
    private String username;
    @Column(name = "nickname", length = 100, unique = true)
    private String nickname;

    @Column(unique = true)
    private String email;

    private String description;

    private String password; // 암호화 필요.

    private LocalDateTime withdrawAt;

    @Enumerated(EnumType.STRING)
    private UserSignUpType signupType;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private UserStatusType userStatus;

    // 1:N mapping
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Feed> feedList = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Reply> replyList = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Rereply> rereplyList = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Follow> followList = new ArrayList<>();

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
