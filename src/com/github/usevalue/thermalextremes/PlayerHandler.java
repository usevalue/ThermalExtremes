package com.github.usevalue.thermalextremes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.logging.Level;


public class PlayerHandler implements Listener {

    private HashMap<Player,ThermalPlayer> playerMap;

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
        if(t==null) return;

        //  Check block environment
        Location l = p.getLocation();
        boolean shaded = p.getWorld().getHighestBlockAt(l).getY()>l.getY();
        boolean watery = l.getBlock().getType().equals(Material.WATER);
        if(watery) t.wetness=10;
        boolean warmed = l.getBlock().getLightFromBlocks()>=ThermalExtremes.configuration.block_lightLevel_warmed;
        boolean heated = l.getBlock().getLightFromBlocks()>=ThermalExtremes.configuration.block_lightLevel_heated;


        double blockTemp = l.getBlock().getTemperature();  // Minecraft-provided stat combining information on altitude and biome
        double lightFromBlocks = l.getBlock().getLightFromBlocks();
        double lightFromSky = l.getBlock().getLightFromSky();
        //  Set measure degree of exposure
        double degreeOfExposure=0;
        switch(ThermalExtremes.clock.checkTemp()) {
            case NORMAL:
                break;
            case HOT:
                degreeOfExposure=lightFromSky+lightFromBlocks;
                t.isExposed=!(shaded||watery);
                break;
            case COLD:
                degreeOfExposure=-1;
                if(t.wetness>0) degreeOfExposure*=t.wetness;
                degreeOfExposure+=lightFromBlocks;
                t.isExposed = degreeOfExposure<0;
        }
        ThermalExtremes.plugin.debug("Degree of exposure for "+p.getDisplayName()+" is "+degreeOfExposure+".  BlockTemp "+blockTemp+", lightfromblocks "+lightFromBlocks+", lightfromsky "+lightFromSky+".");


        // Update temperature
        if(t.isExposed) t.expose(degreeOfExposure/100);
        else t.regulate();

        // Update bodily condition?  If so, notify player.
        if(!watery&&heated) t.wetness--;
        if(!watery&&warmed) t.wetness--;

        if(t.updateBodilyCondition()) {
            if(t.isExposed) {
                if(ThermalExtremes.clock.checkTemp().equals(Temperature.COLD)) {
                    p.sendMessage("You are dangerously exposed to the cold!  Your condition is "+t.condition+"!  Go inside and sit by the fire!");
                }
                else p.sendMessage("You've been over-exposed to the heat!  Your condition is "+t.condition+"!  Find some shade or go for a swim to cool down!");
            }
            else {
                p.sendMessage("You're recoving somewhat.  Your condition is "+t.condition+".");
            }
        }

        // Run effects of bodily condition

    }

}
