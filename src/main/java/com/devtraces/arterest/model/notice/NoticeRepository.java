package com.devtraces.arterest.model.notice;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Integer countAllByNoticeOwnerId(Long noticeOwnerId);
}
