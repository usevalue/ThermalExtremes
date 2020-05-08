package com.github.usevalue.thermalextremes.thermalcreature;

import com.github.usevalue.thermalextremes.Clock;
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

    public Player player;
    public boolean debugging = false;

    // Body state
    private double personalTemp_degrees_C;
    public int wetness;
    private int hydration;
    public BodilyCondition condition;

    // Current location
    public Block standingOn;
    public boolean ventilatedPlace;
    public boolean warmedPlace;
    public boolean sunlitPlace;
    public boolean outsidePlace;
    public boolean wateryPlace;
    public boolean exposed = false;
    private boolean sweating = false;

    public ThermalPlayer(Player p) {
        this.player=p;
        this.personalTemp_degrees_C=idealTemp;
        this.hydration=ThermalConfig.max_hydration;
        this.wetness=0;
        updateBodilyCondition();
        updatePlace();
    }

    public ThermalPlayer(Player p, double personalTemp, int wetness, int hydration) {
        this.player = p;
        this.personalTemp_degrees_C = personalTemp;
        this.wetness = wetness;
        this.hydration = hydration;
        updateBodilyCondition();
        updatePlace();
    }

    public void updatePlace() {
        standingOn = player.getLocation().getBlock();
        sunlitPlace = (standingOn.getLightFromSky() > ThermalConfig.sky_light_sunny);
        outsidePlace = (standingOn.getLightFromSky()==15);
        warmedPlace = (standingOn.getLightFromBlocks()>ThermalConfig.block_light_heated);
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

    public int getHydration() { return hydration; }

    public boolean expose(double degree, Temperature impact) {
        double change;
        if(impact.equals(HOT)) change = degree*ThermalConfig.hot_temp_per_tick;
        else if(impact.equals(COLD)) change = -1*degree*ThermalConfig.cold_temp_per_tick;
        else return false;
        personalTemp_degrees_C+=change;
        if(debugging) player.sendMessage("Exposure causes "+change+"°C change to "+personalTemp_degrees_C);
        if(personalTemp_degrees_C>50) personalTemp_degrees_C = 50;
        if(personalTemp_degrees_C<5) personalTemp_degrees_C = 10;
        return true;
    }

    public BodilyCondition updateBodilyCondition() {
        BodilyCondition target;
        if(personalTemp_degrees_C>=ThermalConfig.severe_hyperthermia_degrees_C) target = BodilyCondition.SEVERE_HYPERTHERMIA;
        else if(personalTemp_degrees_C>= ThermalConfig.hyperthermia_degrees_C) target = BodilyCondition.HYPERTHERMIA;
        else if(personalTemp_degrees_C>=ThermalConfig.comfort_max_C) target = BodilyCondition.OVERHEATED;
        else if(personalTemp_degrees_C>ThermalConfig.comfort_min_C) target = BodilyCondition.COMFORTABLE;
        else if(personalTemp_degrees_C>ThermalConfig.hypothermia_degrees_C) target = BodilyCondition.UNCOMFORTABLY_COLD;
        else if(personalTemp_degrees_C>=ThermalConfig.severe_hypothermia_degrees_C) target = BodilyCondition.HYPOTHERMIA;
        else target=BodilyCondition.SEVERE_HYPOTHERMIA;
        this.condition=target;
        return target;
    }

    public void regulate() {

        int hydrationBefore = 100*hydration/ThermalConfig.max_hydration;

        double regulation = ThermalConfig.base_bodily_regulation;
        if(personalTemp_degrees_C==idealTemp) regulation = 0;
        else if(personalTemp_degrees_C>idealTemp) { //  Too warm
            regulation*=-1;
            if(condition.severity>0||sweating) if(!sweat(condition.severity)&& Clock.random.nextDouble()>0.9) player.damage(0.5);
            if(wetness>0) regulation*=1+((double)wetness/ThermalConfig.max_wetness);
            if(!ventilatedPlace) regulation/=2;
        }
        else {  // Too cold
            if(wetness>0) regulation/=1+((double)wetness/ThermalConfig.max_wetness);
        }
        personalTemp_degrees_C+=regulation;
        if(debugging&&regulation!=0) player.sendMessage("Regulation changed temperature by "+regulation+"°C to "+personalTemp_degrees_C);
        if((regulation>0&&personalTemp_degrees_C>=idealTemp) ||(regulation<0&&personalTemp_degrees_C<=idealTemp)) { // Correct for overshoot
            personalTemp_degrees_C = idealTemp;
            sweating=false;
        }

        double hydrationAfter = 100*hydration/ThermalConfig.max_hydration;
        if(hydrationBefore>=60) {
            if(hydrationAfter<60) player.sendMessage(ChatColor.AQUA+"You're a bit thirsty.");
        }
        else if (hydrationBefore>=40) {
            if(hydrationAfter<40) player.sendMessage(ChatColor.GREEN+"You're getting pretty dehydrated.  Get a bottle of water.");
        }
        else if (hydrationBefore>=20) {
            if(hydrationAfter<20) player.sendMessage(ChatColor.GOLD+"You are dehydrated!  Drink a bottle of water!");
        }
        else if (hydrationBefore>=10) {
            if(hydrationAfter<10) player.sendMessage(ChatColor.RED+"You are severely dehydrated!  Get water or you may die!");
        }

    }

    // For events

    public void hydrate(int amount) {
        hydration+=amount;
        if(hydration>ThermalConfig.max_hydration) hydration=ThermalConfig.max_hydration;
    }

    public void work(float amount) {
        personalTemp_degrees_C += amount*ThermalConfig.heating_from_work;
    }

    private boolean thirst(int amount) {
        hydration -= amount;
        if(hydration>0) return true;
        hydration=0;
        return false;
    }

    private boolean sweat(int amount) {
        if(!sweating) {
            player.sendMessage("You've broken out in a sweat.  It'll cool you down, but remember to stay hydrated.");
            sweating=true;
        }
        if(hydration>=amount) {
            hydration-=amount*ThermalConfig.sweating_hydration_cost;
            wetness+=amount*ThermalConfig.sweating_wetness_bonus;
            return true;
        }
        hydration=0;
        return false;
    }

    public String hydrationBar() {
        StringBuilder s = new StringBuilder(ChatColor.GRAY+ "{ ");
        double hydrationInterval = (double)ThermalConfig.max_hydration/20;
        for(int x=0; x<20; x++) {
            ChatColor c;
            if(hydration>0&&hydration/hydrationInterval>=x) c = ChatColor.BLUE;
            else c = ChatColor.WHITE;
            s.append(c).append(c).append("=");
        }
        s.append(ChatColor.GRAY).append(" }");
        return s.toString();
    }

    public String getWetnessDescription() {
        String s;
        double section = (double)ThermalConfig.max_wetness/4;
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
