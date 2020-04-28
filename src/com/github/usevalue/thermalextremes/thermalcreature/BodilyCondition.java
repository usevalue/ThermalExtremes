package com.github.usevalue.thermalextremes.thermalcreature;

import com.github.usevalue.thermalextremes.ThermalExtremes;
import com.github.usevalue.thermalextremes.thermalcreature.ThermalCreature;
import org.bukkit.ChatColor;

public enum BodilyCondition {
        SEVERE_HYPOTHERMIA("freezing cold", "frostbitten", "!", ChatColor.BLACK,1),
        HYPOTHERMIA("cold", "extremely cold", "!", ChatColor.BLUE, 2),
        UNCOMFORTABLY_COLD("chill", "cold", ".", ChatColor.AQUA,3),
        COMFORTABLE("warmth", "comfortable",".", ChatColor.WHITE,4),
        UNCOMFORTABLY_WARM("heat", "hot", ".", ChatColor.YELLOW,5),
        HYPERTHERMIA("sweltering heat", "overheated","!", ChatColor.GOLD,6),
        SEVERE_HYPERTHERMIA("murderous heat", "suffering from heatstroke","!", ChatColor.RED,7);

        public String effectName;
        public String descriptor;
        public String punctuation;
        public ChatColor color;
        public int ordinalTemp;

        BodilyCondition(String effectName, String descriptor, String punctuation, ChatColor c, int ordinalTemp) {
            this.effectName = effectName;
            this.descriptor = descriptor;
            this.punctuation = punctuation;
            this.color=c;
            this.ordinalTemp = ordinalTemp;
        }
}