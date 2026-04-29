package com.juviai.leave.converter;

public interface Converter<S, T> {
    T convert(S source);
}
