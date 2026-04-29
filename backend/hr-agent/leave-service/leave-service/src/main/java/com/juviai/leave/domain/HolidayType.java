package com.juviai.leave.domain;

public enum HolidayType {
    PUBLIC,       // mandatory national holiday (e.g. Republic Day, Diwali)
    RESTRICTED,   // employee can pick from a list (e.g. regional festivals)
    OPTIONAL
}
