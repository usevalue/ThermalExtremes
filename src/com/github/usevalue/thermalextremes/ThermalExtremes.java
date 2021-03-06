package com.github.usevalue.thermalextremes;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThermalExtremes extends JavaPlugin {

    public static ThermalExtremes plugin;
    public static Logger logger = Bukkit.getLogger();
    public static PlayerHandler playerHandler;
    public static ThermalCommands commands;
    public static Clock clock;
    public static boolean debugMode;
    private ThermalConfig config;

    @Override
    public void onEnable() {
        plugin = this;
        config = new ThermalConfig();
        clock = new Clock();
        playerHandler = new PlayerHandler();
        getServer().getPluginManager().registerEvents(playerHandler, this);
        commands = new ThermalCommands();
        this.getCommand("thermal").setExecutor(commands);
        this.getCommand("body").setExecutor(commands);
        logger.log(Level.INFO, "[ThermalExtremes] Plugin enabled.  Drink plenty of water.");

    }

    @Override
    public void onDisable() {
        playerHandler.saveFile();
    }

    public static void debug(String m) {
        if(debugMode) logger.log(Level.INFO, m);
    }

    public void reloadTheConfigs() {
        config = new ThermalConfig();
        debugMode = ThermalConfig.debug;
        clock.loadClockConfigs();
    }
}
