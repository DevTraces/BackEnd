package com.devtraces.arterest.service;

import com.devtraces.arterest.domain.bookmark.Bookmark;
import com.devtraces.arterest.domain.bookmark.BookmarkRepository;
import com.devtraces.arterest.dto.GetBookmarkListResponseDto;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookmarkService {

	private final BookmarkRepository bookmarkRepository;

	@Transactional
	public List<GetBookmarkListResponseDto> getBookmarkList(Long userId, Integer page, Integer pageSize) {

		Pageable pageable = PageRequest.of(page, pageSize);

		return bookmarkRepository.findByUserId(userId, pageable)
			.stream().map(Bookmark -> GetBookmarkListResponseDto.from(Bookmark))
			.collect(Collectors.toList());
	}

	@Transactional
	public void createBookmark(Long userId, Long feedId) {

		bookmarkRepository.save(
			Bookmark.builder()
				.feedId(feedId)
				.userId(userId)
				.build()
		);
	}

	public void deleteBookmark(Long userId, Long feedId) {

		bookmarkRepository.deleteByUserIdAndFeedId(userId, feedId);
	}
}
