package com.devtraces.arterest.service;

import static com.devtraces.arterest.common.exception.ErrorCode.USER_NOT_FOUND;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.domain.bookmark.Bookmark;
import com.devtraces.arterest.domain.bookmark.BookmarkRepository;
import com.devtraces.arterest.domain.user.User;
import com.devtraces.arterest.domain.user.UserRepository;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookmarkService {

	private final UserRepository userRepository;
	private final BookmarkRepository bookmarkRepository;

	@Transactional
	public void createBookmark(String email, Long feedId) {

		User user = validateUser(email);

		bookmarkRepository.save(
			Bookmark.builder()
				.feedId(feedId)
				.userId(user.getId())
				.build()
		);
	}

	public void deleteBookmark(String email, Long feedId) {

		User user = validateUser(email);

		bookmarkRepository.deleteByUserIdAndFeedId(user.getId(), feedId);
	}

	private User validateUser(String email) {
		return userRepository.findByEmail(email).orElseThrow(
			() -> new BaseException(USER_NOT_FOUND)
		);
	}
}
