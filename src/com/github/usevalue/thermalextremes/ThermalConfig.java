package com.github.usevalue.thermalextremes;
import org.bukkit.configuration.Configuration;
public class ThermalConfig {

    public static long interval;
    public static boolean random_weather;
    public static double stability;
    public static boolean debug;

    public static double base_bodily_regulation;
    public static double comfort_max_C;
    public static double comfort_min_C;
    public static double hypothermia_degrees_C;
    public static double severe_hypothermia_degrees_C;
    public static double hyperthermia_degrees_C;
    public static double severe_hyperthermia_degrees_C;

    public static double lightLevel_warmed;
    public static double block_lightLevel_heated;

    public static double hot_temp_per_tick;
    public static double heatwave_severity;
    public static double heatwave_min_duration;
    public static double heatwave_max_duration;
    public static double cold_temp_per_tick;
    public static double coldsnap_severity;
    public static double coldsnap_min_duration;
    public static double coldsnap_max_duration;


    public ThermalConfig() {
        ThermalExtremes.plugin.saveDefaultConfig();
        Configuration c = ThermalExtremes.plugin.getConfig();
        random_weather = c.getBoolean("random_weather");
        interval = c.getLong("clock_interval");
        stability = c.getDouble("stability");
        debug = c.getBoolean("debug");
        base_bodily_regulation = c.getDouble("base_bodily_regulation");
        comfort_max_C = c.getDouble("comfort_max_C");
        comfort_min_C = c.getDouble("comfort_min_C");
        hypothermia_degrees_C = c.getDouble("hypothermia_degrees_C");
        severe_hypothermia_degrees_C = c.getDouble("severe_hypothermia_degrees_C");
        hyperthermia_degrees_C = c.getDouble("hyperthermia_degrees_C");
        severe_hyperthermia_degrees_C = c.getDouble("severe_hyperthermia_degrees_C");
        lightLevel_warmed = c.getDouble("lightLevel_warmed");
        block_lightLevel_heated = c.getDouble("block_lightLevel_heated");
        hot_temp_per_tick = c.getDouble("heatwave.base_degree_change_when_exposed");
        heatwave_severity = c.getDouble("heatwave.severity");
        heatwave_min_duration = c.getDouble("heatwave.minimum_duration");
        heatwave_max_duration = c.getDouble("heatwave.maximum_duration");
        cold_temp_per_tick = c.getDouble("coldsnap.base_degree_change_when_exposed");
        coldsnap_severity = c.getDouble("coldsnap.severity");
        coldsnap_min_duration = c.getDouble("coldsnap.minimum_duration");
        coldsnap_max_duration = c.getDouble("coldsnap.maximum_duration");

    }

}
