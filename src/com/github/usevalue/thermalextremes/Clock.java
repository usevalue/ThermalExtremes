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
                    chance = duration / plugin.configuration.stability;
                    ThermalExtremes.debug("Global temperatures are normal.  Current chance of thermal event is " + chance + "%.  The dice roll is " + diceRoll);
                    if (chance > diceRoll) {
                        duration = 0;
                        boolean coinFlip = (random.nextDouble() >= 0.5);
                        if (coinFlip)
                            beginHeatwave();
                        else
                            beginColdSnap();
                    }
                    break;
                default:
                    chance = duration / plugin.configuration.severity;
                    ThermalExtremes.debug("Global temperatures are " + temp + ".  Current chance of normalisation is " + chance + "%.  The dice roll is " + diceRoll);
                    if (chance > diceRoll) {
                        duration = 0;
                        normaliseTemps();
                    }
                    break;
            }
        }
        else ThermalExtremes.debug("Global temperatures have been "+temp+" for "+duration+".  Random weather is disabled.");
        // Check player thermal situations
        for(Player p : plugin.getServer().getOnlinePlayers()) {
            plugin.playerHandler.updatePlayer(p);
        }

    }

    public boolean beginHeatwave() {
        temp = HOT;
        plugin.getServer().broadcastMessage("An unbearable heatwave has begun.");
        return true;
    }

    public boolean beginColdSnap() {
        temp = COLD;
        plugin.getServer().broadcastMessage("Temperatures have begun dropping dramatically.");
        return true;
    }

    public boolean normaliseTemps() {
        temp = NORMAL;
        plugin.getServer().broadcastMessage("Temperatures are starting to return to normal levels.");
        return true;
    }

    public Temperature checkTemp() {
        return temp;
    }
}
