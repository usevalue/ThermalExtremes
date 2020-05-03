package com.github.usevalue.thermalextremes;

import com.github.usevalue.thermalextremes.thermalcreature.BodilyCondition;
import com.github.usevalue.thermalextremes.thermalcreature.ThermalPlayer;

import org.bukkit.ChatColor;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.logging.Level;

import static com.github.usevalue.thermalextremes.Temperature.COLD;


public class PlayerHandler implements Listener {

    private HashMap<Player, ThermalPlayer> playerMap;

    public PlayerHandler() {
        playerMap = new HashMap<>();
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
        ThermalExtremes.logger.log(Level.INFO, "Player "+player.getDisplayName()+" removed from tracking.");
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

    public HashMap<Player,ThermalPlayer> getThermalPlayers() {
        return playerMap;
    }

    public ThermalPlayer getThermalPlayer(Player player) {
        return playerMap.get(player);
    }

    public void updatePlayers(Temperature temp) {
        for(Player p : ThermalExtremes.plugin.getServer().getOnlinePlayers()) {
            ThermalExtremes.playerHandler.updatePlayer(p, temp);
        }
    }

    private void updatePlayer(Player p, Temperature temp) {

        // 1.  Check that they exist
        if(p==null) return;
        ThermalPlayer t = getThermalPlayer(p);
        if (t == null) return;
        BodilyCondition currentCondition = t.condition;

        long time = p.getWorld().getTime();
        ThermalExtremes.logger.log(Level.INFO, "The time is " + time);
        WeatherType currentWeather = p.getPlayerWeather();

        // 2.  Player statuses

        // Clothes dry
        if(!t.wateryPlace&&t.wetness>0) {
            double drying = 0;
            drying+=p.getLocation().getBlock().getLightLevel()*2; //  Stand in the light to dry your clothes.
            if(!temp.equals(COLD)) drying += p.getLocation().getBlock().getLightFromSky();
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

        boolean currentlyExposed = t.exposed;

        double exposure = 0;

        switch (temp) {
            case HOT:
                exposure=t.standingOn.getLightFromSky()-ThermalConfig.sky_light_sunny;
                if(p.getLocation().getY()>=p.getWorld().getHighestBlockAt(p.getLocation()).getY()) exposure+=2;
                break;
            case COLD:
                if (t.wateryPlace) {
                    exposure = 10;
                    break;
                }
                else exposure = 1;
                exposure *= 1+((float) t.wetness/100);
                if(p.getLocation().getBlock().getLightFromBlocks()>=ThermalConfig.block_light_heated) exposure--;
            default:
                break;
        }

        if(currentlyExposed!=t.exposed) {
            if(t.exposed) {
                String because;
                if(temp.equals(COLD)&&t.wateryPlace) because = "you're standing in water";
                else if(t.sunlitPlace) because = "you're exposed to the elements";
                else if(!t.ventilatedPlace) because = "this place has no ventilation";
                else because = "you're just unlucky";
                p.sendMessage("You're exposed to the extreme "+temp.cause+" because " + because + ".");
            }
            else {
                p.sendMessage("You've found a shaded but well-ventilated space.  You can recover here.");
            }
        }

        t.exposed = exposure>0;
        if(t.exposed) t.expose(exposure, temp);

        //  REGULATE BODILY TEMP
        double regulation = 1;
        t.regulate(regulation*ThermalConfig.base_bodily_regulation);

        //  5.  Update and notify
        BodilyCondition newBod = t.updateBodilyCondition();
        if(!currentCondition.equals(newBod)) {
            if (currentCondition.severity < newBod.severity) {
                p.sendMessage("Warning!  Because of " + temp.cause + ", you are now " + t.condition.color + t.condition.effectName + ChatColor.WHITE + ".");
            } else {
                p.sendMessage(ChatColor.AQUA+"You're recovering somewhat.  " + t.condition.color + "You're " + t.condition.effectName + " now.");
            }
        }

    }

}
