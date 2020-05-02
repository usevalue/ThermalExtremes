package com.github.usevalue.thermalextremes;

import com.github.usevalue.thermalextremes.thermalcreature.BodilyCondition;
import com.github.usevalue.thermalextremes.thermalcreature.ThermalPlayer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.logging.Level;


public class PlayerHandler implements Listener {

    private HashMap<Player, ThermalPlayer> playerMap;

    public PlayerHandler() {
        playerMap = new HashMap<>();
    }

    @EventHandler
    public void onPlayerJoin(PlayerLoginEvent event) {
        playerMap.putIfAbsent(event.getPlayer(), new ThermalPlayer());
        ThermalExtremes.logger.log(Level.INFO,
                event.getPlayer().getDisplayName()+" has logged in with a body temperature of "+playerMap.get(event.getPlayer()).getTemp()+" and a "+playerMap.get(event.getPlayer()).condition+" bodily condition.");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerMap.remove(player);
        ThermalExtremes.logger.log(Level.INFO, "Player "+player.getDisplayName()+" removed from tracking.");
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

    public HashMap<Player,ThermalPlayer> getThermalPlayers() {
        return playerMap;
    }

    public ThermalPlayer getThermalPlayer(Player player) {
        return playerMap.get(player);
    }


    public void updatePlayer(Player p, Temperature temp) {

        // 1.  Check that they exist
        if(p==null) return;
        ThermalPlayer t = getThermalPlayer(p);
        if (t == null) return;
        BodilyCondition currentCondition = t.condition;


        // 2.  Player statuses

        // Wetness

        boolean watery = p.getLocation().getBlock().getType().equals(Material.WATER);
        if (watery) {
            if(t.wetness<ThermalConfig.max_wetness) p.sendMessage(ChatColor.AQUA+"You got your clothes all wet.");
            t.wetness = ThermalConfig.max_wetness;
        }
        else if(t.wetness>0) {
            double drying = 0;
            drying+=p.getLocation().getBlock().getLightLevel()*2; //  Stand in the light to dry your clothes.
            if(!temp.equals(Temperature.COLD)) drying += p.getLocation().getBlock().getLightFromSky();
            if(temp.equals(Temperature.HOT)) drying*=2;
            t.wetness -= drying;
            if(t.wetness<=0) {
                t.wetness=0;
                p.sendMessage("Your clothes have dried off.");
            }
        }

        // Dehydration

        double hBefore = t.getHydrationPercent();
        if(temp.equals(Temperature.HOT)) t.thirst(2);
        double hAfter = t.getHydrationPercent();
        if(hBefore>=60) {
            if(hAfter<60) p.sendMessage(ChatColor.AQUA+"You're a bit thirsty.");
        }
        else if (hBefore>=40) {
            if(hAfter<40) p.sendMessage(ChatColor.GREEN+"You're getting pretty dehydrated.  Get a bottle of water.");
        }
        else if (hBefore>=20) {
            if(hAfter<20) p.sendMessage(ChatColor.GOLD+"You are dehydrated!  Drink a bottle of water!");
        }
        else if (hBefore>=10) {
            if(hAfter<10) p.sendMessage(ChatColor.RED+"You are severely dehydrated!  Get water or you may die!");
        }

        // 3.  CALCULATE EXPOSURE
        double exposure = 0;

        switch (temp) {
            case HOT:
                exposure=p.getLocation().getBlock().getLightFromSky()-ThermalConfig.sky_light_sunny;
                if(p.getLocation().getY()>=p.getWorld().getHighestBlockAt(p.getLocation()).getY()) exposure+=2;
                break;
            case COLD:
                if (watery) {
                    exposure = 10;
                    break;
                }
                else exposure = 1;
                exposure *= 1+((float) t.wetness/100);
                if(p.getLocation().getBlock().getLightFromBlocks()>=ThermalConfig.block_light_heated) exposure--;
            default:
                break;
        }

        ThermalExtremes.debug("Degree of exposure for " + p.getDisplayName() + " is " + exposure + ".");

        // EXPOSE TO TEMPERATURE CHANGE
        if(exposure>1) t.expose(exposure, temp);

        //  REGULATE BODILY TEMP
        double regulation = 1;
        t.regulate(regulation*ThermalConfig.base_bodily_regulation);

        //  5.  Update and notify
        BodilyCondition newBod = t.updateBodilyCondition();
        if(currentCondition.equals(newBod)) return;
        else if(currentCondition.severity<newBod.severity) {
            p.sendMessage("Warning!  Because of "+temp.cause+", you are now "+t.condition.color+t.condition.effectName+ChatColor.WHITE+".");
        }
        else {
            p.sendMessage("You're recovering somewhat in the "+temp+" weather.  "+t.condition.color+"You're "+t.condition.effectName+" now.");
        }

    }

}
