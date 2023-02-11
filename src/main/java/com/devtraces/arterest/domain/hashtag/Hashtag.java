package com.devtraces.arterest.domain.hashtag;

import com.devtraces.arterest.domain.feed.Feed;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(value = {AuditingEntityListener.class})
@Table(name = "hashtag")
public class Hashtag {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "hashtag_id")
	private Long id;

	private String hashtag;

	@CreatedDate
	private LocalDateTime createdAt;

	// N:1 mapping with Feed
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "feed_id")
	@ToString.Exclude
	private Feed feed;
}
