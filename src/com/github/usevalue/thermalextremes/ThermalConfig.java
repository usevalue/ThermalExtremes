package com.github.usevalue.thermalextremes;
import org.bukkit.configuration.Configuration;
public class ThermalConfig {

    public static long interval;
    public static double risk;
    public static double stability;
    public static double severity;
    public static boolean debug;


    public static double comfort_max_C;
    public static double comfort_min_C;
    public static double hypothermia_degrees_C;
    public static double severe_hypothermia_degrees_C;
    public static double hyperthermia_degrees_C;
    public static double severe_hyperthermia_degrees_C;

    public static double block_lightLevel_warmed;
    public static double block_lightLevel_heated;

    public ThermalConfig() {
        ThermalExtremes.plugin.saveDefaultConfig();
        Configuration c = ThermalExtremes.plugin.getConfig();
        interval = c.getLong("clock_interval");
        risk = c.getDouble("risk");
        stability = c.getDouble("stability");
        severity = c.getDouble("severity");
        debug = c.getBoolean("debug");
        comfort_max_C = c.getDouble("comfort_max_C");
        comfort_min_C = c.getDouble("comfort_min_C");
        hypothermia_degrees_C = c.getDouble("hypothermia_degrees_C");
        severe_hypothermia_degrees_C = c.getDouble("severe_hypothermia_degrees_C");
        hyperthermia_degrees_C = c.getDouble("hyperthermia_degrees_C");
        severe_hyperthermia_degrees_C = c.getDouble("severe_hyperthermia_degrees_C");
        block_lightLevel_warmed = c.getDouble("block_lightLevel_warmed");
        block_lightLevel_heated = c.getDouble("block_lightLevel_heated");
    }

}
