package com.github.usevalue.thermalextremes;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThermalExtremes extends JavaPlugin {

    public static Logger logger = Bukkit.getLogger();
    public static ThermalExtremes plugin;
    public static ThermalConfig configuration;
    public static PlayerHandler playerHandler;
    public static ThermalCommands commands;
    public static Clock clock;

    @Override
    public void onEnable() {
        plugin = this;
        configuration = new ThermalConfig();
        clock = new Clock();
        playerHandler = new PlayerHandler();
        getServer().getPluginManager().registerEvents(playerHandler, this);
        commands = new ThermalCommands();
        this.getCommand("thermal").setExecutor(commands);
        logger.log(Level.INFO, "[ThermalExtremes] Plugin enabled.  Drink plenty of water.");
    }

    @Override
    public void onDisable() {

    }

    public static void debug(String m) {
        if(configuration.debug) logger.log(Level.INFO, m);
    }

}
