package com.devtraces.arterest.domain.feedhashtagmap;

import com.devtraces.arterest.common.domain.BaseEntity;
import com.devtraces.arterest.domain.feed.Feed;
import com.devtraces.arterest.domain.hashtag.Hashtag;
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
@Table(name = "feed_hashtag_map")
@Entity
public class FeedHashtagMap extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "heed_hashtag_map_id")
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