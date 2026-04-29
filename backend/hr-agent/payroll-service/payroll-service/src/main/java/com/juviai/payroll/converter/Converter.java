package com.juviai.payroll.converter;

public interface Converter<S, T> {
    T convert(S source);
}
