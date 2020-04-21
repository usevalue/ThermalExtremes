package com.github.usevalue.thermalextremes;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.logging.Level;

import static com.github.usevalue.thermalextremes.Temperature.*;

public class PlayerHandler implements Listener {

    private HashMap<Player,ThermalPlayer> playerMap;
    private int chattiness;
    private int chatCounter = 0;

    public PlayerHandler() {
        playerMap = new HashMap<Player,ThermalPlayer>();
        chattiness = ThermalExtremes.configuration.playerhandler_chattiness;
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
        ThermalExtremes.plugin.getLogger().log(Level.INFO, "Player "+player.getDisplayName()+" removed from Thermal tracking.");
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

        //  Determine environmental exposure
        switch(ThermalExtremes.clock.checkTemp()) {
            case NORMAL:
                t.exposure=0;
                break;
            case HOT:
                break;
            case COLD:
                break;
        }

        // Bodily exposure

        switch(ThermalExtremes.clock.checkTemp()) {
            case NORMAL:
                break;
            case HOT:
                break;
            case COLD:
                break;
        }



        // Regulate or expose
        double tempshift = 0;
        
        if(t.exposure>0) {

        }
        else {

        }

        // Harm the player

        // Notify player

        p.sendMessage("Hey you!");




    }

}
