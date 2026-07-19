package com.penguincrushers;

import lombok.Getter;

@Getter
public enum CrossingStatus
{
    SAFE_TO_CROSS("Safe to Cross"),
    UNSAFE_TO_CROSS("Unsafe to Cross"),
    CROSSING_SAFELY("Crossing Safely"),
    CROSSING_UNSAFELY("Crossing Unsafely");

    private final String name;

    CrossingStatus(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
