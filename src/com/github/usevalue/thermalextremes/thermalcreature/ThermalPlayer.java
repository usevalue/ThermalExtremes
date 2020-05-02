package com.github.usevalue.thermalextremes.thermalcreature;

import com.github.usevalue.thermalextremes.Temperature;
import com.github.usevalue.thermalextremes.ThermalConfig;
import org.bukkit.ChatColor;

import static com.github.usevalue.thermalextremes.Temperature.COLD;
import static com.github.usevalue.thermalextremes.Temperature.HOT;

public class ThermalPlayer extends ThermalCreature {
    private double personalTemp_degrees_C;
    public int wetness; // Out of 300
    private int hydration; // Out of 200
    public BodilyCondition condition;
    public static final double idealTemp = (ThermalConfig.comfort_min_C+ThermalConfig.comfort_max_C)/2;

    // private list of conditions

    public ThermalPlayer() {
        personalTemp_degrees_C = idealTemp; // Healthy temp in Celsius
        wetness = 0;
        hydration = Math.floorDiv(ThermalConfig.max_hydration, 2);
        updateBodilyCondition();
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

    public boolean expose(double degree, Temperature impact) {
        if(impact.equals(HOT)) {
            personalTemp_degrees_C += degree*ThermalConfig.hot_temp_per_tick;
        }
        if(impact.equals(COLD)) {
            personalTemp_degrees_C -= degree*ThermalConfig.cold_temp_per_tick;
        }
        if(personalTemp_degrees_C>50) personalTemp_degrees_C = 50;
        if(personalTemp_degrees_C<5) personalTemp_degrees_C = 10;
        return true;
    }

    public boolean thirst(int amount) {
        hydration -= amount;
        if(hydration>0) return true;
        hydration=0;
        return false;
    }

    public String hydrationBar() {
        StringBuilder s = new StringBuilder(ChatColor.GRAY+ "{ ");
        double hydrationInterval = ThermalConfig.max_hydration/20;
        for(int x=0; x<20; x++) {
            ChatColor c;
            if(hydration>0&&hydration/hydrationInterval>=x) c = ChatColor.BLUE;
            else c = ChatColor.WHITE;
            s.append(c).append(c+"=");
        }
        s.append(ChatColor.GRAY+" }");
        return s.toString();
    }

    public float getHydration() {
        return hydration;
    }

    public double getHydrationPercent() {
        return 100*(float)hydration/ ThermalConfig.max_hydration;
    }

    public void hydrate(int amount) {
        hydration+=amount;
        if(hydration>ThermalConfig.max_hydration) hydration=ThermalConfig.max_hydration;
    }

}
