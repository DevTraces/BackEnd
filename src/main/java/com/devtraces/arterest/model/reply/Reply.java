package com.devtraces.arterest.model.reply;


import com.devtraces.arterest.common.model.BaseEntity;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.rereply.Rereply;
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
@Table(name = "reply")
@Entity
public class Reply extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id")
    private Long id;

    private Long feedPrimaryKeyId;
    private Integer numberOfRereplies;

    private String content;

    // 1:N mapping with User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    // 1:N mapping with Feed
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id")
    @ToString.Exclude
    private Feed feed;

    // 1:N mapping with Rereply
    @OneToMany(mappedBy = "reply")
    @ToString.Exclude
    private List<Rereply> rereplyList = new ArrayList<>();

    public void updateContent(String content){
        this.content = content;
    }

    public void plusOneRereply(){
        this.numberOfRereplies ++;
    }

    public void minusOneRereply(){
        this.numberOfRereplies --;
    }
}

