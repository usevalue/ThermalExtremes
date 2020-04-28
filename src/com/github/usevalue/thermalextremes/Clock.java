package com.github.usevalue.thermalextremes;

import org.bukkit.entity.Player;
import java.util.Random;

import static com.github.usevalue.thermalextremes.Temperature.*;

public class Clock extends org.bukkit.scheduler.BukkitRunnable {

    public static Clock clock;
    private Random random;
    private int duration;
    private Temperature temp = Temperature.NORMAL;
    private ThermalExtremes plugin = com.github.usevalue.thermalextremes.ThermalExtremes.plugin;
    public boolean randomWeather;

    public Clock() {
        clock=this;
        random = new Random();
        duration = 0;
        randomWeather = ThermalExtremes.configuration.random_weather;
        runTaskTimer(plugin, 5, plugin.configuration.interval);
    }

    @Override
    public void run() {
        duration+=plugin.configuration.interval;

        // Should the temperature change?
        if(randomWeather) {
            double diceRoll = 100 * random.nextDouble();
            double chance;
            switch (temp) {
                case NORMAL:
                    chance = duration / ThermalConfig.stability;
                    ThermalExtremes.debug("Global temperatures are normal.  Current chance of thermal event is " + chance + "%.  The dice roll is " + diceRoll);
                    if (chance > diceRoll) beginRandomWeather();
                    break;
                case HOT:
                    if(ThermalConfig.heatwave_min_duration>0 && duration < ThermalConfig.heatwave_min_duration) {
                        ThermalExtremes.debug("Heatwave has persisted for "+duration+" server ticks.  Minimum "+ThermalConfig.heatwave_min_duration+" for possible change is ticks.");
                        break;
                    }
                    if(ThermalConfig.heatwave_max_duration>0 && duration >= ThermalConfig.heatwave_max_duration) {
                        ThermalExtremes.debug("Heatwave has reached max duration of "+ThermalConfig.heatwave_max_duration+" server ticks and is ending.  To disable, set max heatwave duration to 0.");
                        normaliseTemps();
                        break;
                    }
                    ThermalExtremes.debug("Rolling to end heatwave: rolled "+diceRoll+" against "+duration/ThermalConfig.heatwave_severity);
                    if(duration / ThermalConfig.heatwave_severity > diceRoll) normaliseTemps();
                    break;
                case COLD:
                    if(ThermalConfig.coldsnap_min_duration>0 && duration < ThermalConfig.coldsnap_min_duration) {
                        ThermalExtremes.debug("Cold snap has persisted for "+duration+" server ticks.  Minimum "+ThermalConfig.coldsnap_min_duration+" for possible change is ticks.");
                        break;
                    }
                    if(ThermalConfig.coldsnap_max_duration>0 && duration >= ThermalConfig.coldsnap_max_duration) {
                        ThermalExtremes.debug("Cold snap has reached max duration of "+ThermalConfig.coldsnap_max_duration+" server ticks and is ending.  To disable, set max cold snap duration to 0.");
                        normaliseTemps();
                        break;
                    }
                    ThermalExtremes.debug("Rolling to end cold snap: rolled "+diceRoll+" against "+duration/ThermalConfig.coldsnap_severity);
                    if(duration / ThermalConfig.heatwave_severity > diceRoll) normaliseTemps();
                    break;
            }
        }
        else ThermalExtremes.debug("Global temperatures have been "+temp+" for "+duration+".  Random weather is disabled.");

        // Check player thermal situations
        for(Player p : plugin.getServer().getOnlinePlayers()) {
            plugin.playerHandler.updatePlayer(p, temp);
        }

    }

    public boolean beginRandomWeather() {
        boolean coinFlip = (random.nextDouble() >= 0.5);
        if (coinFlip)
            return beginHeatwave();
        else
            return beginColdSnap();
    }

    public boolean beginHeatwave() {
        if(temp.equals(HOT)) return false;
        temp = HOT;
        duration=0;
        plugin.getServer().broadcastMessage("An unbearable heatwave has begun.");
        return true;
    }

    public boolean beginColdSnap() {
        if(temp.equals(COLD)) return false;
        temp = COLD;
        duration=0;
        plugin.getServer().broadcastMessage("Temperatures have begun dropping dramatically.");
        return true;
    }

    public boolean normaliseTemps() {
        if(temp.equals(NORMAL)) return false;
        temp = NORMAL;
        duration=0;
        plugin.getServer().broadcastMessage("Temperatures are starting to return to normal levels.");
        return true;
    }

    public Temperature checkTemp() {
        return temp;
    }
}
