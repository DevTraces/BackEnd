package com.devtraces.arterest.domain.feed;

import com.devtraces.arterest.common.domain.BaseEntity;
import com.devtraces.arterest.domain.feedhashtagmap.FeedHashtagMap;
import com.devtraces.arterest.domain.reply.Reply;
import com.devtraces.arterest.domain.user.User;
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

}
