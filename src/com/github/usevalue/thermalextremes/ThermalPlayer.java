package com.github.usevalue.thermalextremes;

public class ThermalPlayer {
    private double personalTemp_degrees_C;
    public int exposure;

    // private list of conditions

    public ThermalPlayer() {
        personalTemp_degrees_C = (ThermalExtremes.configuration.comfort_max_C+ThermalExtremes.configuration.comfort_min_C)/2; // Healthy temp in Celsius
    }

    public double getTemp() {
        return personalTemp_degrees_C;
    }

    public boolean setTemp(double target) {
        if(personalTemp_degrees_C==target) return false;
        personalTemp_degrees_C = target;
        return true;
    }

}
