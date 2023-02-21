package com.devtraces.arterest.model.rereply;

import java.util.Collection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RereplyRepository extends JpaRepository<Rereply, Long> {

    Slice<Rereply> findAllByReplyId(Long replyId, PageRequest pageRequest);

    void deleteAllByIdIn(Collection<Long> id);

}
