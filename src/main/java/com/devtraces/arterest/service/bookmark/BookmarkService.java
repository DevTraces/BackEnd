package com.devtraces.arterest.service.bookmark;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.bookmark.dto.response.GetBookmarkListResponse;
import com.devtraces.arterest.model.bookmark.Bookmark;
import com.devtraces.arterest.model.bookmark.BookmarkRepository;
import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.feed.FeedRepository;
import com.devtraces.arterest.model.user.User;
import com.devtraces.arterest.model.user.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

	private final BookmarkRepository bookmarkRepository;
	private final UserRepository userRepository;
	private final FeedRepository feedRepository;

	@Transactional(readOnly = true)
	public List<GetBookmarkListResponse> getBookmarkList(Long userId, Integer page, Integer pageSize) {

		Pageable pageable = PageRequest.of(page, pageSize);

		return bookmarkRepository.findByUserId(userId, pageable)
			.stream().map(Bookmark -> GetBookmarkListResponse.from(Bookmark))
			.collect(Collectors.toList());
	}

	@Transactional
	public void createBookmark(Long userId, Long feedId) {

		User user = userRepository.findById(userId)
			.orElseThrow(() -> BaseException.USER_NOT_FOUND);

		Feed feed = feedRepository.findById(feedId)
			.orElseThrow(() -> BaseException.FEED_NOT_FOUND);

		bookmarkRepository.save(
			Bookmark.builder()
				.user(user)
				.feed(feed)
				.build()
		);
	}

	public void deleteBookmark(Long userId, Long feedId) {

		bookmarkRepository.deleteByUserIdAndFeedId(userId, feedId);
	}
}
