package com.github.usevalue.thermalextremes;

public enum Temperature {
    HOT("the heat"),
    NORMAL("your body"),
    COLD("the cold");

    public String cause;

    Temperature(String cause) {
        this.cause=cause;
    }
}
