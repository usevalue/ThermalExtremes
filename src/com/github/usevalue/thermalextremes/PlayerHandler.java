package com.github.usevalue.thermalextremes;

import com.github.usevalue.thermalextremes.thermalcreature.BodilyCondition;
import com.github.usevalue.thermalextremes.thermalcreature.ThermalPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.logging.Level;

import static com.github.usevalue.thermalextremes.Temperature.NORMAL;


public class PlayerHandler implements Listener {

    private HashMap<Player, ThermalPlayer> playerMap;

    public PlayerHandler() {
        playerMap = new HashMap<Player,ThermalPlayer>();
    }

    @EventHandler
    public void onPlayerJoin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        ThermalPlayer thermalPlayer = addPlayer(player);
    }

    private ThermalPlayer addPlayer(Player p) {
        ThermalPlayer thermalPlayer = new ThermalPlayer();
        playerMap.putIfAbsent(p, thermalPlayer);
        ThermalExtremes.plugin.getLogger().log(Level.INFO, "Now tracking "+p.getDisplayName()+" with a body temp of "+thermalPlayer.getTemp()+" C.");
        return playerMap.get(p);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerMap.remove(player);
        ThermalExtremes.plugin.getLogger().log(Level.INFO, "Player "+player.getDisplayName()+" removed from tracking.");
    }

    public HashMap<Player,ThermalPlayer> getThermalPlayers() {
        return playerMap;
    }

    public ThermalPlayer getThermalPlayer(Player player) {
        ThermalPlayer t = playerMap.get(player);
        if(t==null) return addPlayer(player);
        else return t;
    }

    // updatePlayer is called for each player each plugin tick.
    // First, it verifies the player and their record.
    // Second, it update's the player's hydration levels.
    // Third, it calculates the player's exposure to thermal extremes.
    // Fourth, it adjusts the player's temperature based on exposure versus their body's capacity to regulate.
    // Fifth, it update's the player's bodily condition and returns the result to the clock.

    public BodilyCondition updatePlayer(Player p, Temperature temp) {

        // 1.  Check that they exist
        ThermalPlayer t = getThermalPlayer(p);

        if (t == null) return null;
        // 2.  Update hydration
        // Water exposure
        boolean watery = p.getLocation().getBlock().equals(Material.WATER);
        if (watery) {
            t.wetness = 200;
        }
        else if(t.wetness>0) t.wetness -= p.getLocation().getBlock().getLightLevel();
        // Dehydration
        boolean hydrated = t.hydration > 0;

        // 3.  Calculate exposure
        double exposure = 0;
        if (temp.equals(NORMAL)) {
            if(t.condition.ordinalTemp<4) { // Represents fine weather, but player is nevertheless hypothermic.
                exposure += t.wetness/10;
                if(p.getLocation().getBlock().getLightLevel()<7) exposure++;  // Harder to warm up in the shade
            }
            else if(t.condition.ordinalTemp>4) {  // Fine weather, but hyperthermic
                exposure -= t.wetness/10;
                if(p.getLocation().getBlock().getLightLevel()>6) exposure++;  // Harder to cool down in the sun.
                if(!hydrated) exposure*=2;
                if(watery) exposure/=2;
            }
        }
        else switch (temp) {
            case HOT:
                if (watery) break;
                exposure++;
                exposure += p.getLocation().getBlock().getLightFromSky();
                exposure -= t.wetness/10;
                break;
            case COLD:
                if (watery) {
                    exposure = 10;
                    break;
                }
                else exposure = 1;
                exposure*=t.wetness/10;
                exposure-=p.getLocation().getBlock().getLightFromBlocks();
            default:
                break;
        }

        ThermalExtremes.debug("Degree of exposure for " + p.getDisplayName() + " is " + exposure + ".");


        //  4.  Adjust temperature based on exposure



        if(exposure>0) {
            double change=0;
            switch(temp) {
                case NORMAL:
                    if(t.condition.ordinalTemp<4) change=exposure*ThermalConfig.cold_temp_per_tick*-1;
                    else change=exposure*ThermalConfig.hot_temp_per_tick;
                    break;
                case HOT:
                    change=exposure*ThermalConfig.hot_temp_per_tick;
                    break;
                case COLD:
                    change=exposure*ThermalConfig.cold_temp_per_tick;
            }
            t.expose(change);
        }


        t.regulate(ThermalConfig.base_bodily_regulation);

        //  5.  Update and return bodily condition.
        return t.updateBodilyCondition();

    }

}
