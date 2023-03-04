package com.devtraces.arterest.model.feed;

import com.devtraces.arterest.common.model.BaseEntity;
import com.devtraces.arterest.model.feedhashtagmap.FeedHashtagMap;
import com.devtraces.arterest.model.reply.Reply;
import com.devtraces.arterest.model.user.User;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "feed")
@Entity
public class Feed extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_id")
    private Long id;

    private String content;

    private String hashtagStringValues;
    private Integer numberOfReplies;

    @Column(length = 6000) // S3에서 전달하는 이미지 url 길이가 255 바이트보다 길어서 저장가능 제한을 확장해줘야 함.
    private String imageUrls;

    // 1:N mapping with User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    // 1:N mapping with Reply
    @OneToMany(mappedBy = "feed")
    @ToString.Exclude
    List<Reply> replyList = new ArrayList<>();

    // 1:N mapping with FeedHashtagMap
    @OneToMany(mappedBy = "feed")
    @ToString.Exclude
    List<FeedHashtagMap> feedHashtagMapList = new ArrayList<>();

    public void updateContent(String content){
        this.content = content;
    }

    public void updateImageUrls(String imageUrls){
        this.imageUrls = imageUrls;
    }

    public void updateHashtagStringValues(String input){
        this.hashtagStringValues = input;
    }

    public void plusOneReply(){
        this.numberOfReplies ++;
    }

    public void minusOneReply(){
        this.numberOfReplies --;
    }

}
