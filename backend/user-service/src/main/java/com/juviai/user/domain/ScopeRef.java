package com.juviai.user.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable value object identifying a single scope: its {@link ScopeType}
 * and the entity UUID inside that type. For {@link ScopeType#GLOBAL} the
 * {@code id} is {@code null}.
 */
public final class ScopeRef {

    private final ScopeType type;
    private final UUID id;

    private ScopeRef(ScopeType type, UUID id) {
        this.type = Objects.requireNonNull(type, "ScopeType is required");
        this.id = id;
    }

    public static ScopeRef of(ScopeType type, UUID id) {
        return new ScopeRef(type, id);
    }

    public static ScopeRef global() {
        return new ScopeRef(ScopeType.GLOBAL, null);
    }

    public ScopeType type() {
        return type;
    }

    public UUID id() {
        return id;
    }

    public boolean isGlobal() {
        return type == ScopeType.GLOBAL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScopeRef that)) return false;
        return type == that.type && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }

    @Override
    public String toString() {
        return isGlobal() ? "GLOBAL" : type + ":" + id;
    }
}
