package com.devtraces.arterest.model.feedhashtagmap;

import com.devtraces.arterest.common.model.BaseEntity;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.hashtag.Hashtag;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
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
@Table(
    name = "feed_hashtag_map",
    uniqueConstraints={
        @UniqueConstraint(
            name="feed_id_hastag_id_unique",
            columnNames={"feed_id", "hashtag_id"}
        )
    }
)
@Entity
public class FeedHashtagMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_hashtag_map_id")
    private Long id;

    // 1:N mapping with Feed
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id")
    @ToString.Exclude
    private Feed feed;

    // 1:N mapping with Hashtag
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id")
    @ToString.Exclude
    private Hashtag hashtag;

}
