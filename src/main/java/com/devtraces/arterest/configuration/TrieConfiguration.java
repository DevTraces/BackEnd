package com.devtraces.arterest.configuration;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TrieConfiguration {
	@Bean
	public Trie<String, String> trie() {
		return new PatriciaTrie<>();
	}
}
