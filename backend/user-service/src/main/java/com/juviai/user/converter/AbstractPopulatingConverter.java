package com.juviai.user.converter;

import java.util.List;

public abstract class AbstractPopulatingConverter<S, T> implements Converter<S, T> {

    private final List<Populator<S, T>> populators;
    private final Class<? extends T> targetClass;

    protected AbstractPopulatingConverter(List<? extends Populator<S, T>> populators, Class<? extends T> targetClass) {
        this.populators = List.copyOf(populators);
        this.targetClass = targetClass;
    }

    @Override
    public T convert(S source) {
        if (source == null) {
            return null;
        }

        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            for (Populator<S, T> populator : populators) {
                populator.populate(source, target);
            }
            return target;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
