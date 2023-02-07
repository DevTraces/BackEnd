package com.devtraces.arterest.domain.reply;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    void deleteAllByIdIn(Collection<Long> id);

}
