package com.github.usevalue.thermalextremes;

public class ThermalConfig {

    public static long interval;
    public static double risk;
    public static double stability;
    public static double severity;
    public static boolean debug;

    public static int playerhandler_chattiness;
    public static double comfort_max_C;
    public static double comfort_min_C;
    public static double hypothermia_degrees_C;
    public static double severe_hypothermia_degrees_C;
    public static double hyperthermia_degrees_C;
    public static double severe_hyperthermia_degrees_C;

    public ThermalConfig() {
        interval = 10;
        risk = 20;
        stability = 50;
        severity = 30;
        debug=false;
        comfort_max_C = 37;
        comfort_min_C = 38;
        hypothermia_degrees_C = 35;
        severe_hypothermia_degrees_C = 28;
        hyperthermia_degrees_C = 38.3;
        severe_hyperthermia_degrees_C = 40;
        playerhandler_chattiness=3;
    }

}
