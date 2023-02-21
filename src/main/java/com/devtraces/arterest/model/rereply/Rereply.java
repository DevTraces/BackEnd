package com.devtraces.arterest.model.rereply;

import com.devtraces.arterest.common.model.BaseEntity;
import com.devtraces.arterest.model.reply.Reply;
import com.devtraces.arterest.model.user.User;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "rereply")
@Entity
public class Rereply extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rereply_id")
    private Long id;

    private String content;

    // 1:N mapping with User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    // 1:N mapping with Reply
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_id")
    @ToString.Exclude
    private Reply reply;

    public void updateContent(String content){
        this.content = content;
    }
}
