package com.github.usevalue.thermalextremes;

import com.github.usevalue.thermalextremes.thermalcreature.ThermalPlayer;
import org.bukkit.Location;
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

    public void updatePlayer(Player p) {

        // Check that they exist
        ThermalPlayer t = getThermalPlayer(p);
        if (t == null) return;
        if (!ThermalExtremes.clock.checkTemp().equals(NORMAL)) {
            //  Check block environment
            Location l = p.getLocation();
            boolean sunlit = p.getWorld().getHighestBlockAt(l).getY() <= l.getY();
            boolean warmed = l.getBlock().getLightFromBlocks() >= ThermalExtremes.configuration.lightLevel_warmed;
            boolean heated = l.getBlock().getLightFromBlocks() >= ThermalExtremes.configuration.block_lightLevel_heated;
            boolean watery = l.getBlock().getType().equals(Material.WATER);
            if (watery) t.wetness = 30;
            double blockTemp = l.getBlock().getTemperature();  // Minecraft-provided stat combining information on altitude and biome

            //  Set measure degree of exposure
            double degreeOfExposure = 0;

            switch (ThermalExtremes.clock.checkTemp()) {
                case HOT:
                    if (watery) {
                        degreeOfExposure = 0;
                        break;
                    }
                    degreeOfExposure = 1;
                    if (sunlit) degreeOfExposure *= 2;
                    if (warmed) degreeOfExposure *= 1.1;
                    if (heated) degreeOfExposure *= 1.5;
                    if (t.wetness > 0) degreeOfExposure *= (1 - t.wetness / 100);
                    break;
                case COLD:
                    if (watery) {
                        degreeOfExposure = 10;
                        break;
                    } else degreeOfExposure = 1;
                    if (t.wetness > 0) degreeOfExposure *= 1 + (t.wetness / 10);
                    if (heated) {
                        degreeOfExposure *= 0.5;
                        t.wetness-=2;
                    }
                    t.isExposed = degreeOfExposure < 0;
                default:
                    break;
            }
            t.isExposed = degreeOfExposure > 1;

            ThermalExtremes.plugin.debug("Degree of exposure for " + p.getDisplayName() + " is " + degreeOfExposure + ".  BlockTemp " + blockTemp + ", sunlit: " + sunlit);

            // Update temperature
            if (t.isExposed) t.expose(degreeOfExposure);
            else t.regulate(0.1/degreeOfExposure);
        }

        if(t.updateBodilyCondition()) {
            if(t.isExposed) p.sendMessage(t.condition.color.toString()+"You are exposed to the "+t.condition.effectName+".  You are "+t.condition.descriptor+" now"+t.condition.punctuation);
        }
    }

}
