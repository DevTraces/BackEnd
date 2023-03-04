package com.devtraces.arterest.model.notice;

import com.devtraces.arterest.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Integer countAllByNoticeOwnerId(Long noticeOwnerId);

    Page<Notice> findALlByNoticeOwnerId(Long noticeOwnerId, PageRequest pageRequest);

    void deleteAllByNoticeOwnerId(Long noticeOwnerId);

    void deleteAllByUser(User user);

    List<Notice> findAllByNoticeOwnerIdAndUserId(Long noticeOwnerId, Long userId);
}
