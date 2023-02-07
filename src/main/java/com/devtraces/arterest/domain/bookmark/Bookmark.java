package com.devtraces.arterest.domain.bookmark;

import com.devtraces.arterest.common.domain.BaseEntity;
import com.devtraces.arterest.domain.feed.Feed;
import com.devtraces.arterest.domain.user.User;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(value = {AuditingEntityListener.class})
@Table(name = "bookmark")
public class Bookmark {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "bookmark_id")
	private Long id;

	private Long feedId;
	private Long userId;

	@CreatedDate
	private LocalDateTime createdAt;

	// N:1 mapping with User
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	@ToString.Exclude
	private User user;

	// N:1 mapping with Feed
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "feed_id")
	@ToString.Exclude
	private Feed feed;

}
