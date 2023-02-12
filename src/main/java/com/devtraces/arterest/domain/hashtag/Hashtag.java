package com.devtraces.arterest.domain.hashtag;

import com.devtraces.arterest.common.domain.BaseEntity;
import com.devtraces.arterest.domain.feedhashtagmap.FeedHashtagMap;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "hashtag")
@Entity
public class Hashtag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hashtag_id")
    private Long id;

    private String hashtag;

    // 1:N mapping with FeedHashtagMap
    @OneToMany(mappedBy = "hashtag")
    @ToString.Exclude
    List<FeedHashtagMap> feedHashtagMapList = new ArrayList<>();

}
