package com.github.usevalue.thermalextremes;

import com.github.usevalue.thermalextremes.thermalcreature.BodilyCondition;
import com.github.usevalue.thermalextremes.thermalcreature.ThermalPlayer;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.logging.Level;

import static com.github.usevalue.thermalextremes.Temperature.COLD;
import static com.github.usevalue.thermalextremes.Temperature.HOT;


public class PlayerHandler implements Listener {

    private HashMap<Player, ThermalPlayer> playerMap;
    private long time;

    public PlayerHandler() {
        playerMap = new HashMap<>();
        for(Player p : ThermalExtremes.plugin.getServer().getOnlinePlayers())
            playerMap.putIfAbsent(p, new ThermalPlayer(p));
    }

    public HashMap<Player,ThermalPlayer> getThermalPlayers() {
        return playerMap;
    }

    public ThermalPlayer getThermalPlayer(Player player) {
        return playerMap.get(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerLoginEvent event) {
        playerMap.putIfAbsent(event.getPlayer(), new ThermalPlayer(event.getPlayer()));
        ThermalExtremes.logger.log(Level.INFO,
                event.getPlayer().getDisplayName()+" has logged in with a body temperature of "+playerMap.get(event.getPlayer()).getTemp()+" and a "+playerMap.get(event.getPlayer()).condition+" bodily condition.");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerMap.remove(player);
        ThermalExtremes.debug("Player "+player.getDisplayName()+" removed from tracking.");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        ThermalPlayer t = getThermalPlayer(event.getPlayer());
        t.updatePlace();
    }

    @EventHandler
    public void drinkingWater(PlayerItemConsumeEvent event) {
        if(event.getItem().getItemMeta() instanceof PotionMeta) {
            if(((PotionMeta) event.getItem().getItemMeta()).getBasePotionData().getType() == PotionType.WATER) {
                ThermalPlayer t = playerMap.get(event.getPlayer());
                t.hydrate(ThermalConfig.water_bottle_hydration);
                event.getPlayer().sendMessage(t.hydrationBar());
            }
        }
    }

    @EventHandler
    public void playerWorking(BlockBreakEvent event) {
        ThermalPlayer t = getThermalPlayer(event.getPlayer());
        t.work(event.getBlock().getType().getHardness());
    }

    public void updatePlayers(Temperature temp) {
        for(Player p : ThermalExtremes.plugin.getServer().getOnlinePlayers()) {
            updatePlayer(p, temp);
        }
    }

    private void updatePlayer(Player p, Temperature temp) {


        // 1.  Get Thermal Player
        ThermalPlayer t = getThermalPlayer(p);
        if (t == null) return;

        time = p.getWorld().getTime();
        boolean stormy = p.getWorld().hasStorm();
        BodilyCondition currentCondition = t.condition;


        // 2.  Player statuses

        // Clothes drying
        if(t.wateryPlace) {
            t.wetness=ThermalConfig.max_wetness;
        } else if(t.outsidePlace&&stormy) {
            t.wetness+=6;
        } else if(t.wetness>0) {
            double drying = 1;
            if(t.warmedPlace) drying+=1+t.standingOn.getLightFromBlocks()-ThermalConfig.block_light_heated; //  Stand by the fire to dry your clothes.
            if(!temp.equals(COLD)) drying += (float)t.standingOn.getLightFromSky()/2;
            if(temp.equals(Temperature.HOT)) drying*=2;
            t.wetness -= Math.floor(drying);
            if(t.wetness<=0) {
                t.wetness=0;
                if(t.sweating) t.sweating=false;
                p.sendMessage("Your clothes have dried off.");
            }
        }

        // 3.  CALCULATE WEATHER EXPOSURE

        boolean currentlyExposed = t.exposed;
        String because = "you're just unlucky.";
        double exposure = 0;
        if(!t.ventilatedPlace) {
            exposure+=0.01;
            because = "this place has no ventilation";
        }

        switch (temp) {
            case HOT:
                double radiantHeat = 1;
                if (t.sunlitPlace) {
                    if(time<12000) {
                        because = "of the blazing hot sun.";
                        exposure+=2;
                        if(t.outsidePlace) exposure++;
                    }
                    else {
                        exposure += radiantHeat;  // Radiant heat > 0
                        because = "of the stiflingly hot air.";
                    }
                }
                else if(!t.ventilatedPlace) exposure++;
                if(stormy) exposure/=4;
                break;
            case COLD:
                // Wet
                if(t.wateryPlace) {
                    exposure=5;
                    because = "of your watery situation.";
                    break;
                }
                if(t.sunlitPlace) {
                    exposure+=t.standingOn.getLightFromSky()-ThermalConfig.sky_light_sunny;
                    because = "you are exposed to the elements.";
                }
                if(t.wetness>0.1*ThermalConfig.max_wetness) {
                    exposure *= 0.2 + ((double) t.wetness) / ThermalConfig.max_wetness;
                    because = "of your wet clothes.";
                }
                if(time>13000) {
                    because = "of the freezing night";
                    exposure*=3;
                }
                break;
            default:
                break;
        }

        if(t.debugging&&exposure>0) p.sendMessage(temp+" WEATHER: your exposure is "+exposure);

        t.exposed = exposure>0;

        if(t.exposed!=currentlyExposed) {
            if(t.exposed) p.sendMessage("You're exposed to the elements because "+because);
            else p.sendMessage("You've found a sheltered, well-ventilated place where you can recover.");
        }


        //  5. Change body temperature

        if(t.exposed) t.expose(exposure, temp);


        //  6.  Bodily conditions

        BodilyCondition newBod = t.updateBodilyCondition();
        if(!currentCondition.equals(newBod)) {
            if (currentCondition.severity < newBod.severity) {
                p.sendMessage("Warning!  Because of " + temp.cause + ", you are now " + t.condition.color + t.condition.effectName + ChatColor.WHITE + ".");
            } else {
                p.sendMessage(ChatColor.AQUA+"You're recovering somewhat.  " + t.condition.color + "You're " + t.condition.effectName + " now.");
            }
        }

        if(t.condition.severity>1) {
            if(t.condition.risk.equals(COLD)) {

            }
            else if(t.condition.risk.equals(HOT)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,300,t.condition.severity));
            }
        }

        // 7.   Player's body temperature regulates, assuming they're still alive.
        if(p.getHealth()>0) t.regulate();


    }

}
