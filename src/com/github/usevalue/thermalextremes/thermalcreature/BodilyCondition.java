package com.github.usevalue.thermalextremes.thermalcreature;
import com.github.usevalue.thermalextremes.Temperature;
import org.bukkit.ChatColor;

import static com.github.usevalue.thermalextremes.Temperature.*;

public enum BodilyCondition {
        SEVERE_HYPOTHERMIA("frostbitten",  ChatColor.BLACK,1,3, COLD),
        HYPOTHERMIA("extremely cold", ChatColor.BLUE, 2,2, COLD),
        UNCOMFORTABLY_COLD("cold",  ChatColor.AQUA,3,1, COLD),
        COMFORTABLE("comfortable", ChatColor.WHITE,4,0, NORMAL),
        UNCOMFORTABLY_WARM("hot", ChatColor.YELLOW,5,1,HOT),
        HYPERTHERMIA("overheated", ChatColor.GOLD,6,2, HOT),
        SEVERE_HYPERTHERMIA("suffering from heatstroke", ChatColor.RED,7,3,HOT);

        public String effectName;
        public ChatColor color;
        public int ordinalTemp;
        public int severity;
        public Temperature risk;

        BodilyCondition(String effectName, ChatColor c, int ordinalTemp, int severity, Temperature risk) {
            this.effectName = effectName;
            this.color=c;
            this.ordinalTemp = ordinalTemp;
            this.severity=severity;
            this.risk=risk;
        }
}