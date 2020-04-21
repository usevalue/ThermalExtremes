package com.github.usevalue.thermalextremes;


import org.bukkit.entity.Player;

import java.util.Random;
import java.util.logging.Level;

import static com.github.usevalue.thermalextremes.Temperature.*;

public class Clock extends org.bukkit.scheduler.BukkitRunnable {

    public static Clock clock;
    private Random random;
    private int duration;
    private boolean tick = true;
    private Temperature temp = Temperature.NORMAL;
    private ThermalExtremes plugin = com.github.usevalue.thermalextremes.ThermalExtremes.plugin;

    public Clock() {
        clock=this;
        random = new Random();
        duration = 0;
        runTaskTimer(plugin, 5, plugin.configuration.interval);
    }

    @Override
    public void run() {
        duration+=plugin.configuration.interval;
        // Should the temperature change?
        double diceRoll = 100*random.nextDouble();
        double chance;
        switch(temp) {
            case NORMAL:
                chance = duration/plugin.configuration.stability;
                ThermalExtremes.debug("Current chance of thermal event is "+chance+"%.  The dice roll is "+diceRoll);
                if(chance>diceRoll) {
                    duration=0;
                    boolean coinFlip = (random.nextDouble()>=0.5);
                    if(coinFlip)
                        beginHeatwave();
                    else
                        beginColdSnap();
                }
                break;
            default:
                chance = duration/plugin.configuration.severity;
                ThermalExtremes.debug("Temperatures are unusually "+temp+".  Chance of normalisation is "+chance+".  The dice roll is "+diceRoll);
                if(chance>diceRoll) {
                    duration=0;
                    normaliseTemps();
                }
                break;
        }

        // Report temp to console in debug mode
        if(plugin.configuration.debug) {
            switch (temp) {
                case HOT:
                    plugin.getLogger().log(Level.INFO, "It's very warm.");
                    break;
                case COLD:
                    plugin.getLogger().log(Level.INFO, "It's very cold.");
                    break;
                case NORMAL:
                    plugin.getLogger().log(Level.INFO, "The temperature is agreeable.");
                    break;
            }
        }
        // Check player thermal situations
        for(Player p : plugin.getServer().getOnlinePlayers()) {
            plugin.playerHandler.updatePlayer(p);
        }

    }

    public boolean beginHeatwave() {
        temp = HOT;
        plugin.getLogger().log(Level.INFO, "The heat is becoming unbearable!");
        plugin.getServer().broadcastMessage("An unbearable heatwave has begun.");
        return true;
    }

    public boolean beginColdSnap() {
        temp = Temperature.COLD;
        plugin.getLogger().log(Level.INFO, "Temperatures are dropping dramatically.");
        plugin.getServer().broadcastMessage("Temperatures have begun dropping dramatically.");
        return true;
    }

    public boolean normaliseTemps() {
        temp = Temperature.NORMAL;
        plugin.getLogger().log(Level.INFO, "Temperatures are returning to normal.");
        plugin.getServer().broadcastMessage("Temperatures are starting to return to normal levels.");
        return true;
    }

    public Temperature checkTemp() {
        return temp;
    }
}
