package com.github.usevalue.thermalextremes.thermalcreature;

import com.github.usevalue.thermalextremes.ThermalConfig;

public class ThermalPlayer extends ThermalCreature {
    private double personalTemp_degrees_C;
    public int wetness = 0;
    public int hydration = 100;
    public BodilyCondition condition;
    public boolean isExposed=false;
    public static final double idealTemp = (ThermalConfig.comfort_min_C+ThermalConfig.comfort_max_C)/2;

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

    public BodilyCondition updateBodilyCondition() {
        BodilyCondition target;
        if(personalTemp_degrees_C>=ThermalConfig.severe_hyperthermia_degrees_C) target = BodilyCondition.SEVERE_HYPERTHERMIA;
        else if(personalTemp_degrees_C>= ThermalConfig.hyperthermia_degrees_C) target = BodilyCondition.HYPERTHERMIA;
        else if(personalTemp_degrees_C>=ThermalConfig.comfort_max_C) target = BodilyCondition.UNCOMFORTABLY_WARM;
        else if(personalTemp_degrees_C>ThermalConfig.comfort_min_C) target = BodilyCondition.COMFORTABLE;
        else if(personalTemp_degrees_C>ThermalConfig.hypothermia_degrees_C) target = BodilyCondition.UNCOMFORTABLY_COLD;
        else if(personalTemp_degrees_C>=ThermalConfig.severe_hypothermia_degrees_C) target = BodilyCondition.HYPOTHERMIA;
        else target=BodilyCondition.SEVERE_HYPOTHERMIA;
        condition=target;
        return target;
    }

    public boolean regulate(double d) {  // Returns true if the creature is at its ideal temperature.
        if(personalTemp_degrees_C==idealTemp) return true;
        else if(personalTemp_degrees_C>idealTemp) {
            personalTemp_degrees_C-=d;
            if(personalTemp_degrees_C<=idealTemp) {
                personalTemp_degrees_C=idealTemp;
                return true;
            }
            return false;
        }
        else {
            personalTemp_degrees_C+=d;
            if(personalTemp_degrees_C>idealTemp) {
                personalTemp_degrees_C=idealTemp;
                return true;
            }
            return false;
        }
    }

    public boolean expose(double degree) {
        personalTemp_degrees_C+=degree;
        if(personalTemp_degrees_C>50) personalTemp_degrees_C = 50;
        if(personalTemp_degrees_C<10) personalTemp_degrees_C = 10;
        return true;
    }

}
