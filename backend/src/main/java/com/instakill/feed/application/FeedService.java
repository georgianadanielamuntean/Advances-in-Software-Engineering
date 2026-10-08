package com.instakill.feed.application;

import com.instakill.feed.domain.FeedRankingStrategy;
import com.instakill.feed.domain.FeedSort;
import com.instakill.post.domain.Post;
import com.instakill.post.domain.PostRepository;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class FeedService {

    private final Map<FeedSort, FeedRankingStrategy> strategies = new EnumMap<>(FeedSort.class);
    private final PostRepository posts;

    public FeedService(List<FeedRankingStrategy> strategies, PostRepository posts) {
        for (FeedRankingStrategy strategy : strategies) {
            FeedRankingStrategy previous = this.strategies.putIfAbsent(strategy.sort(), strategy);
            if (previous != null) {
                throw new IllegalStateException("Multiple feed ranking strategies registered for " + strategy.sort());
            }
        }
        this.posts = posts;
    }

    public List<Post> listFeed(FeedSort sort, int page, int size) {
        FeedSort requested = sort != null ? sort : FeedSort.NEW;
        FeedRankingStrategy strategy = strategies.get(requested);
        if (strategy == null) {
            throw new IllegalArgumentException("No feed ranking strategy registered for " + requested);
        }
        return strategy.fetch(posts, page, size);
    }
}
