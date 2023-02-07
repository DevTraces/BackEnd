package com.devtraces.arterest.domain.rereply;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RereplyRepository extends JpaRepository<Rereply, Long> {

    void deleteAllByIdIn(Collection<Long> id);

}
