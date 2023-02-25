package com.devtraces.arterest.model.reply;

import java.util.Collection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {

    Slice<Reply> findAllByFeedIdOrderByCreatedAtDesc(Long feedId, PageRequest pageRequest);

    void deleteAllByIdIn(Collection<Long> id);

}
