package com.instakill.feed.application;

import com.instakill.feed.domain.FeedRankingStrategy;
import com.instakill.feed.domain.FeedSort;
import com.instakill.post.domain.Post;
import com.instakill.post.domain.PostRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedServiceTest {

    private final PostRepository repository = Mockito.mock(PostRepository.class);

    @Test
    void delegatesToStrategyMatchingRequestedSort() {
        List<Post> newest = List.of(post());
        List<Post> trending = List.of(post());
        FeedService service = new FeedService(
                List.of(new FixedStrategy(FeedSort.NEW, newest), new FixedStrategy(FeedSort.TRENDING, trending)),
                repository);

        assertThat(service.listFeed(FeedSort.NEW, 0, 10)).isEqualTo(newest);
        assertThat(service.listFeed(FeedSort.TRENDING, 0, 10)).isEqualTo(trending);
    }

    @Test
    void passesRepositoryAndPagingToStrategy() {
        RecordingStrategy strategy = new RecordingStrategy(FeedSort.TRENDING);
        FeedService service = new FeedService(List.of(strategy), repository);

        service.listFeed(FeedSort.TRENDING, 3, 7);

        assertThat(strategy.repository).isSameAs(repository);
        assertThat(strategy.page).isEqualTo(3);
        assertThat(strategy.size).isEqualTo(7);
    }

    @Test
    void usesAnyRegisteredImplementationWithoutChangesToService() {
        // A brand-new ranking algorithm plugged in for TRENDING
        List<Post> custom = List.of(post(), post());
        FeedService service = new FeedService(List.of(new FixedStrategy(FeedSort.TRENDING, custom)), repository);

        assertThat(service.listFeed(FeedSort.TRENDING, 0, 2)).isEqualTo(custom);
    }

    @Test
    void defaultsToNewWhenSortIsNull() {
        List<Post> newest = List.of(post());
        FeedService service = new FeedService(List.of(new FixedStrategy(FeedSort.NEW, newest)), repository);

        assertThat(service.listFeed(null, 0, 5)).isEqualTo(newest);
    }

    @Test
    void failsWhenNoStrategyRegisteredForSort() {
        FeedService service = new FeedService(List.of(new FixedStrategy(FeedSort.NEW, List.of())), repository);

        assertThatThrownBy(() -> service.listFeed(FeedSort.TRENDING, 0, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TRENDING");
    }

    @Test
    void rejectsDuplicateStrategiesForSameSort() {
        List<FeedRankingStrategy> strategies = List.of(
                new FixedStrategy(FeedSort.NEW, List.of()),
                new FixedStrategy(FeedSort.NEW, List.of()));

        assertThatThrownBy(() -> new FeedService(strategies, repository))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("NEW");
    }

    private Post post() {
        Instant now = Instant.now();
        return new Post(UUID.randomUUID(), UUID.randomUUID(), "content", null, now, now, 0, 0);
    }

    private record FixedStrategy(FeedSort sort, List<Post> result) implements FeedRankingStrategy {
        @Override
        public List<Post> fetch(PostRepository repository, int page, int size) {
            return result;
        }
    }

    private static final class RecordingStrategy implements FeedRankingStrategy {
        private final FeedSort sort;
        private PostRepository repository;
        private int page;
        private int size;

        private RecordingStrategy(FeedSort sort) {
            this.sort = sort;
        }

        @Override
        public FeedSort sort() {
            return sort;
        }

        @Override
        public List<Post> fetch(PostRepository repository, int page, int size) {
            this.repository = repository;
            this.page = page;
            this.size = size;
            return List.of();
        }
    }
}
