package com.devtraces.arterest.domain.rereply;

import java.util.Collection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RereplyRepository extends JpaRepository<Rereply, Long> {

    Slice<Rereply> findAllByReplyId(Long replyId, PageRequest pageRequest);

    void deleteAllByIdIn(Collection<Long> id);

}
