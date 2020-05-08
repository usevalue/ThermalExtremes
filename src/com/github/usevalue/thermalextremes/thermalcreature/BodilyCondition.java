package com.github.usevalue.thermalextremes.thermalcreature;
import com.github.usevalue.thermalextremes.Temperature;
import org.bukkit.ChatColor;

import static com.github.usevalue.thermalextremes.Temperature.*;

public enum BodilyCondition {
        SEVERE_HYPOTHERMIA("frostbitten",  ChatColor.BLACK,1,3, COLD, "getting inside by the fire, quickly"),
        HYPOTHERMIA("hypothermic", ChatColor.BLUE, 2,2, COLD, "going inside and warming up"),
        UNCOMFORTABLY_COLD("cold",  ChatColor.AQUA,3,1, COLD, "to keep an eye on your /body"),
        COMFORTABLE("comfortable", ChatColor.WHITE,4,0, NORMAL, "pacing yourself"),
        OVERHEATED("overheated", ChatColor.YELLOW,5,1,HOT, "to drink plenty of water and take breaks"),
        HYPERTHERMIA("suffering from heat exhaustion", ChatColor.GOLD,6,2, HOT, "to get into the shade and drink water"),
        SEVERE_HYPERTHERMIA("suffering from heatstroke", ChatColor.RED,7,3,HOT, "to make it to shade and water before you die");

        public String effectName;
        public ChatColor color;
        public int ordinalTemp;
        public int severity;
        public Temperature risk;
        public String remedy;

        BodilyCondition(String effectName, ChatColor c, int ordinalTemp, int severity, Temperature risk, String remedy) {
            this.effectName = effectName;
            this.color=c;
            this.ordinalTemp = ordinalTemp;
            this.severity=severity;
            this.risk=risk;
            this.remedy=remedy;
        }
}