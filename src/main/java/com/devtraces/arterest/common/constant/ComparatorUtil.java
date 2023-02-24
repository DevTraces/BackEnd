package com.devtraces.arterest.common.constant;

import com.devtraces.arterest.model.feed.Feed;
import com.devtraces.arterest.model.reply.Reply;
import java.time.LocalDateTime;
import java.util.Comparator;

public class ComparatorUtil {

    public static final class LatestFeedFirstComparator implements Comparator<Feed> {
        @Override
        public int compare(Feed beforeFeed, Feed afterFeed) {
            return afterFeed.getCreatedAt().compareTo(beforeFeed.getCreatedAt());
        }
    }

    public static final class LatestReplyFirstComparator implements Comparator<Reply> {
        @Override
        public int compare(Reply beforeReply, Reply afterReply) {
            return afterReply.getCreatedAt().compareTo(beforeReply.getCreatedAt());
        }
    }

}
