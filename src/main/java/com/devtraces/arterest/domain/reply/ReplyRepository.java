package com.devtraces.arterest.domain.reply;

import java.util.Collection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    Slice<Reply> findAllByFeedId(Long feedId, PageRequest pageRequest);

    void deleteAllByIdIn(Collection<Long> id);

}
