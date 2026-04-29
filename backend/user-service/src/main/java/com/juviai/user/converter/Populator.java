package com.juviai.user.converter;

public interface Populator<S, T> {
    void populate(S source, T target);
}
