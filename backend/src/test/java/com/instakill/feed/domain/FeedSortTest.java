package com.instakill.feed.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeedSortTest {

    @Test
    void parsesKnownValuesIgnoringCase() {
        assertThat(FeedSort.from("trending")).isEqualTo(FeedSort.TRENDING);
        assertThat(FeedSort.from("TRENDING")).isEqualTo(FeedSort.TRENDING);
        assertThat(FeedSort.from("new")).isEqualTo(FeedSort.NEW);
    }

    @Test
    void defaultsToNewForMissingOrUnknownValues() {
        assertThat(FeedSort.from(null)).isEqualTo(FeedSort.NEW);
        assertThat(FeedSort.from("")).isEqualTo(FeedSort.NEW);
        assertThat(FeedSort.from("popular")).isEqualTo(FeedSort.NEW);
    }
}
