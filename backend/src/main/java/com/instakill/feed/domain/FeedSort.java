package com.instakill.feed.domain;

import java.util.Arrays;

public enum FeedSort {
    NEW,
    TRENDING;

    public static FeedSort from(String value) {
        if (value == null) {
            return NEW;
        }
        return Arrays.stream(values())
                .filter(sort -> sort.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(NEW);
    }
}
