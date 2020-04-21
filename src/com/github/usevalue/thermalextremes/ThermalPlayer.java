package com.github.usevalue.thermalextremes;

public class ThermalPlayer {
    private double personalTemp_degrees_C;
    public int wetness = 0;
    public BodilyCondition condition;
    public boolean isExposed=false;
    public static final double idealTemp = (ThermalExtremes.configuration.comfort_max_C+ThermalExtremes.configuration.comfort_min_C)/2;

    // private list of conditions

    public ThermalPlayer() {
        personalTemp_degrees_C = idealTemp; // Healthy temp in Celsius
        condition= BodilyCondition.COMFORTABLE;
    }

    public double getTemp() {
        return personalTemp_degrees_C;
    }

    public boolean setTemp(double target) {
        if(personalTemp_degrees_C==target) return false;
        personalTemp_degrees_C = target;
        return true;
    }

    public boolean updateBodilyCondition() {
        BodilyCondition target;
        if(personalTemp_degrees_C>=ThermalExtremes.configuration.severe_hyperthermia_degrees_C) target = BodilyCondition.SEVERE_HYPERTHERMIA;
        else if(personalTemp_degrees_C>=ThermalExtremes.configuration.hyperthermia_degrees_C) target = BodilyCondition.HYPERTHERMIA;
        else if(personalTemp_degrees_C>=ThermalExtremes.configuration.comfort_max_C) target = BodilyCondition.UNCOMFORTABLY_WARM;
        else if(personalTemp_degrees_C>ThermalExtremes.configuration.comfort_min_C) target = BodilyCondition.COMFORTABLE;
        else if(personalTemp_degrees_C>ThermalExtremes.configuration.hypothermia_degrees_C) target = BodilyCondition.UNCOMFORTABLY_COLD;
        else if(personalTemp_degrees_C>=ThermalExtremes.configuration.severe_hypothermia_degrees_C) target = BodilyCondition.HYPOTHERMIA;
        else target=BodilyCondition.SEVERE_HYPOTHERMIA;

        if(condition.equals(target)) {
            return false;
        }
        else {
            condition = target;
            return true;
        }
    }

    public static enum BodilyCondition {
        SEVERE_HYPOTHERMIA,
        HYPOTHERMIA,
        UNCOMFORTABLY_COLD,
        COMFORTABLE,
        UNCOMFORTABLY_WARM,
        HYPERTHERMIA,
        SEVERE_HYPERTHERMIA
    }

    public boolean regulate() {  // Returns true if the creature is at its ideal temperature.
        if(personalTemp_degrees_C==idealTemp) return true;
        else if(personalTemp_degrees_C>idealTemp) {
            personalTemp_degrees_C--;
            if(personalTemp_degrees_C<=idealTemp) {
                personalTemp_degrees_C=idealTemp;
                return true;
            }
            return false;
        }
        else {
            personalTemp_degrees_C++;
            if(personalTemp_degrees_C>idealTemp) {
                personalTemp_degrees_C=idealTemp;
                return true;
            }
            return false;
        }
    }

    public boolean expose(double degree) {
        personalTemp_degrees_C+=degree;
        return true;
    }

}
