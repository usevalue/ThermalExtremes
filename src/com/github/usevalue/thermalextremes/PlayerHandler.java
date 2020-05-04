package com.github.usevalue.thermalextremes;

import com.github.usevalue.thermalextremes.thermalcreature.BodilyCondition;
import com.github.usevalue.thermalextremes.thermalcreature.ThermalPlayer;

import org.bukkit.ChatColor;
import org.bukkit.WeatherType;
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
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.logging.Level;

import static com.github.usevalue.thermalextremes.Temperature.COLD;


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

    @EventHandler
    public void playerWorking(BlockBreakEvent event) {
        ThermalPlayer t = getThermalPlayer(event.getPlayer());
        int work = getWork(event.getBlock());
        t.work(work);
    }


    private int getWork(Block block) {
        return 1;
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
        BodilyCondition currentCondition = t.condition;
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
                if(!currentWeather.equals(WeatherType.CLEAR)) exposure/=4;
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
                    because="you are exposed to the elements.";
                }
                if(t.wetness>0.1*ThermalConfig.max_wetness)
                    exposure *= 0.2+((double) t.wetness)/ThermalConfig.max_wetness;
                    because = "of your wet clothes.";
                if(time>13000) {
                    because = "of the freezing night";
                    exposure*=3;
                }
                break;
            default:
                break;
        }

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

        // 7.   Player's body temperature regulates, assuming they're still alive.
        if(p.getHealth()>0) t.regulate();
    }

}
