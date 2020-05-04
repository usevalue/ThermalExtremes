package com.github.usevalue.thermalextremes.thermalcreature;

import com.github.usevalue.thermalextremes.Temperature;
import com.github.usevalue.thermalextremes.ThermalConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import static com.github.usevalue.thermalextremes.Temperature.COLD;
import static com.github.usevalue.thermalextremes.Temperature.HOT;

public class ThermalPlayer extends ThermalCreature {

    public static final double idealTemp = (ThermalConfig.comfort_min_C+ThermalConfig.comfort_max_C)/2;

    public final Player player;

    // Body state
    private double personalTemp_degrees_C;
    public int wetness;
    private int hydration;
    public BodilyCondition condition;

    // Current location
    public Block standingOn;
    public boolean ventilatedPlace;
    public boolean sunlitPlace;
    public boolean outsidePlace;
    public boolean wateryPlace;
    public boolean exposed = false;

    public ThermalPlayer(Player p) {
        personalTemp_degrees_C = idealTemp; // Healthy temp in Celsius
        wetness = 0;
        hydration = ThermalConfig.max_hydration;
        player = p;
        updateBodilyCondition();
        updatePlace();
    }

    public void updatePlace() {
        standingOn = player.getLocation().getBlock();
        sunlitPlace = (standingOn.getLightFromSky() > ThermalConfig.sky_light_sunny);
        outsidePlace = (standingOn.getLightFromSky()==15);
        ventilatedPlace = (standingOn.getLightFromSky() > 0);
        wateryPlace = standingOn.getType().equals(Material.WATER);
        if (wateryPlace) {
            if(wetness<ThermalConfig.max_wetness) player.sendMessage(ChatColor.AQUA+"Your clothes got all wet.");
            wetness = ThermalConfig.max_wetness;
        }

    }

    public double getTemp() {
        return personalTemp_degrees_C;
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

    public void regulate() {
        if(personalTemp_degrees_C==idealTemp) return;
        double d = ThermalConfig.base_bodily_regulation;
        if(personalTemp_degrees_C>idealTemp) {
            if(wetness>0) d*=1+(wetness/ThermalConfig.max_wetness);
            personalTemp_degrees_C-=d;
            if(personalTemp_degrees_C<=idealTemp) {
                personalTemp_degrees_C=idealTemp;
            }
        }
        else if(personalTemp_degrees_C<idealTemp){
            if(wetness>0) d/=1+(wetness/ThermalConfig.max_wetness);
            personalTemp_degrees_C+=d;
            if(personalTemp_degrees_C>idealTemp) {
                personalTemp_degrees_C=idealTemp;
            }
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

    public double getHydrationPercent() {
        return 100*(float)hydration/ ThermalConfig.max_hydration;
    }

    public void hydrate(int amount) {
        hydration+=amount;
        if(hydration>ThermalConfig.max_hydration) hydration=ThermalConfig.max_hydration;
    }

    public void work(int amount) {
        personalTemp_degrees_C += amount*ThermalConfig.heating_from_work;
    }

    public String getWetnessDescription() {
        String s;
        double section = ThermalConfig.max_wetness/5;
        int level = (int) Math.ceil(wetness/section);
        switch(level) {
            case 1:
                s = "damp";
                break;
            case 2:
                s = "wet";
                break;
            case 3:
                s = "drenched";
                break;
            case 4:
                s = "soaked";
                break;
            default:
                s = "dry";
                break;
        }
        return s;
    }

}
