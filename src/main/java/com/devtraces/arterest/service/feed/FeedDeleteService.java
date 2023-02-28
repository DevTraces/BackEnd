package com.devtraces.arterest.service.feed;

import com.devtraces.arterest.model.feed.FeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedDeleteService {
	private final FeedRepository feedRepository;

	@Transactional
	public void deleteFeedEntity(Long feedId){
		feedRepository.deleteById(feedId);
	}
}
