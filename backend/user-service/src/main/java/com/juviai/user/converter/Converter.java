package com.juviai.user.converter;

import java.util.ArrayList;
import java.util.List;

public interface Converter<S, T> {
    T convert(S source);

    default List<T> convertAll(Iterable<S> sources) {
        List<T> out = new ArrayList<>();
        if (sources == null) {
            return out;
        }
        for (S s : sources) {
            out.add(convert(s));
        }
        return out;
    }
}
