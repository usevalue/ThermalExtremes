package com.github.usevalue.thermalextremes;

public enum Temperature {
    HOT("the heat"),
    NORMAL("poor working conditions"),
    COLD("the cold");

    public String cause;

    Temperature(String cause) {
        this.cause=cause;
    }
}
