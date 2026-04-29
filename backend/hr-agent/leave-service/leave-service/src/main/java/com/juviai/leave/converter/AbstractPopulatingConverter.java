package com.juviai.leave.converter;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractPopulatingConverter<S, T> implements Converter<S, T> {

    @Override
    public T convert(S source) {
        T target = createTarget();
        populate(source, target);
        return target;
    }

    public List<T> convertAll(List<S> sources) {
        if (sources == null) return List.of();
        return sources.stream().map(this::convert).collect(Collectors.toList());
    }

    protected abstract T createTarget();
    protected abstract void populate(S source, T target);
}
