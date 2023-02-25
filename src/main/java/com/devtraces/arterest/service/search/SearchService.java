package com.devtraces.arterest.service.search;

import com.devtraces.arterest.common.exception.BaseException;
import com.devtraces.arterest.controller.search.dto.response.GetHashtagsSearchResponse;
import com.devtraces.arterest.controller.search.dto.response.GetNicknameSearchResponse;
import com.devtraces.arterest.controller.search.dto.response.GetUsernameSearchResponse;
import com.devtraces.arterest.model.hashtag.Hashtag;
import com.devtraces.arterest.model.hashtag.HashtagRepository;
import com.devtraces.arterest.model.user.UserRepository;
import com.devtraces.arterest.service.search.util.SearchRedisUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {

	private final UserRepository userRepository;
	private final HashtagRepository hashtagRepository;
	private final Trie trie;
	private final SearchRedisUtil searchRedisUtil;
	private final String TRIE_KEY = "trie";


	// 1분 간격으로 게시물 테이블의 모든 해시태그를 파싱하여, Trie 구조로 redis 에 저장함.
	@Transactional
//	@Scheduled(cron = "0 * * * * *")
	public void createAutoCompleteWords() {
		List<Hashtag> hashtagList = hashtagRepository.findAll();
		saveAllHashtags(hashtagList);
	}

	public List<String> getAutoCompleteWords(String keyword, Integer numberOfWords) {
		return getAutoCompleteHashtags(keyword, numberOfWords);
	}

	public GetHashtagsSearchResponse getSearchResultUsingHashtags(
		String keyword, Integer page, Integer pageSize) {

		Optional<Hashtag> hashtag = hashtagRepository.findByHashtagString(keyword);

		if(hashtag.isPresent()){
			return GetHashtagsSearchResponse.from(
				hashtag.get(), page, pageSize);
		}

		return GetHashtagsSearchResponse.builder()
			.totalNumberOfSearches(0L)
			.feedList(null)
			.build();
	}

	public List<GetUsernameSearchResponse> getSearchResultUsingUsername(
		String keyword, Integer page, Integer pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);

		return userRepository.findByUsernameStartsWith(keyword, pageable)
			.stream().map(User -> GetUsernameSearchResponse.from(User))
			.collect(Collectors.toList());
	}

	public List<GetNicknameSearchResponse> getSearchResultUsingNickname(
		String keyword, Integer page, Integer pageSize) {
		Pageable pageable = PageRequest.of(page, pageSize);

		return userRepository.findByNicknameStartsWith(keyword, pageable)
			.stream().map(User -> GetNicknameSearchResponse.from(User))
			.collect(Collectors.toList());
	}

	// 해시태그가 저장된 Trie 자료구조를 직렬화하여 Redis 에 저장.
	private void saveAllHashtags(List<Hashtag> feedList) {
		for (int i = 0; i < feedList.size(); i++) {
			trie.put(feedList.get(i).getHashtagString(), null);
		}

		byte[] serializedTrie;

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
				oos.writeObject(trie);
				serializedTrie = baos.toByteArray();
			}
		} catch (IOException e) {
			log.error(e.getMessage());
			throw BaseException.INTERNAL_SERVER_ERROR;
		}

		String encodingTrie = Base64.getEncoder().encodeToString(serializedTrie);

		searchRedisUtil.setTrieValue(TRIE_KEY, encodingTrie);
	}

	// Redis 에 저장된 Trie 자료구조를 역직렬화하여 자동완성된 단어들을 가져옴.
	public List<String> getAutoCompleteHashtags(String keyword, Integer numberOfWords) {
		String output = searchRedisUtil.getTrieValue(TRIE_KEY);

		byte[] serializedTrie;
		Trie trie = null;

		serializedTrie = Base64.getDecoder().decode(output);
		try (ByteArrayInputStream bais = new ByteArrayInputStream(serializedTrie)) {
			try (ObjectInputStream ois = new ObjectInputStream(bais)) {
				Object objectTrie = ois.readObject();
				trie = (Trie) objectTrie;
			} catch (IOException e) {
				log.error(e.getMessage());
				throw BaseException.INTERNAL_SERVER_ERROR;
			} catch (ClassNotFoundException e) {
				log.error(e.getMessage());
				throw BaseException.INTERNAL_SERVER_ERROR;
			}
		} catch (IOException e) {
			log.error(e.getMessage());
			throw BaseException.INTERNAL_SERVER_ERROR;
		}

		return (List<String>) trie.prefixMap(keyword).keySet()
			.stream()
			.limit(numberOfWords)
			.collect(Collectors.toList());
	}
}
