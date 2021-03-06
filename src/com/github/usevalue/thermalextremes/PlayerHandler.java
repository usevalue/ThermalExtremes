package com.github.usevalue.thermalextremes;

import com.github.usevalue.thermalextremes.thermalcreature.BodilyCondition;
import com.github.usevalue.thermalextremes.thermalcreature.ThermalPlayer;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.regions.CylinderRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import static com.github.usevalue.thermalextremes.Temperature.COLD;
import static com.github.usevalue.thermalextremes.Temperature.HOT;


public class PlayerHandler implements Listener {

    private File playersFile = new File(ThermalExtremes.plugin.getDataFolder(), "thermal_players.yml");
    private YamlConfiguration playerBase;
    private HashMap<String, ThermalPlayer> playerMap;
    private long time;
    private WorldEditPlugin worldEdit;
    private boolean usingWorldEdit = true;

    public PlayerHandler() {

        if(usingWorldEdit) {
            Plugin plug = ThermalExtremes.plugin.getServer().getPluginManager().getPlugin("WorldEdit");
            if(plug instanceof WorldEditPlugin) worldEdit = (WorldEditPlugin) plug;
        }
        if(worldEdit==null) usingWorldEdit = false;
        ThermalExtremes.logger.log(Level.INFO, "Is there worldedit: "+usingWorldEdit);
        try {
            playerBase = new YamlConfiguration();
            if (playersFile.exists()) {
                ThermalExtremes.logger.log(Level.INFO, "Loaded pre-existing file.");
                playerBase.load(playersFile);
            } else {
                ThermalExtremes.logger.log(Level.INFO, "Creating new ThermalPlayers file.");
            }
        }
        catch (Exception e) { ThermalExtremes.logger.log(Level.WARNING, "Problem encountered while trying to load your thermal_players.yml!"); }

        playerMap = new HashMap<>();


        for(Player p : ThermalExtremes.plugin.getServer().getOnlinePlayers()) {
            ThermalPlayer t = null;
            if(playerBase!=null) {
                t = parsePlayerData(p);
            }
            if(t==null) t = new ThermalPlayer(p);
            playerMap.putIfAbsent(p.getDisplayName(), t);
        }
    }

    public boolean savePlayers() {
        for (String name : playerMap.keySet()) savePlayer(name);
        return saveFile();
    }

    public boolean saveFile() {
        try {
            playerBase.save(playersFile);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void savePlayer(String name) {
        ConfigurationSection s = playerBase.getConfigurationSection(name);
        if(s==null) s = playerBase.createSection(name);
        ThermalPlayer t = playerMap.get(name);
        s.set("temperature",t.getTemp());
        s.set("wetness", t.wetness);
        s.set("hydration", t.getHydration());
    }

    private ThermalPlayer parsePlayerData(Player p) {
        if(playerBase.getKeys(false).contains(p.getDisplayName())) {
            ConfigurationSection s = playerBase.getConfigurationSection(p.getDisplayName());
            if(s==null) {
              return null;
            }
            else {
                double tmp = s.getDouble("temperature",ThermalPlayer.idealTemp);
                int wet = s.getInt("wetness",0);
                int hyd = s.getInt("hydration", ThermalConfig.max_hydration);
                return new ThermalPlayer(p, tmp, wet, hyd);
            }
        }
        return null;
    }

    public HashMap<String,ThermalPlayer> getThermalPlayers() {
        return playerMap;
    }

    public ThermalPlayer getThermalPlayer(String name) {
        return playerMap.get(name);
    }

    @EventHandler
    public void onPlayerJoin(PlayerLoginEvent event) {
        ThermalPlayer t = parsePlayerData(event.getPlayer());
        if(t==null) t = new ThermalPlayer(event.getPlayer());
        playerMap.putIfAbsent(event.getPlayer().getDisplayName(), t);
        ThermalExtremes.logger.log(Level.INFO,
                event.getPlayer().getDisplayName()+" has logged in with a body temperature of "+t.getTemp()+" and a "+t.condition+" bodily condition.");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String name = event.getPlayer().getDisplayName();
        savePlayer(name);
        saveFile();
        playerMap.remove(name);
        ThermalExtremes.debug("Player "+name+" saved to file.");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        ThermalPlayer t = getThermalPlayer(event.getPlayer().getDisplayName());
        t.updatePlace();
    }

    @EventHandler
    public void drinkingWater(PlayerItemConsumeEvent event) {
        if(event.getItem().getItemMeta() instanceof PotionMeta) {
            if(((PotionMeta) event.getItem().getItemMeta()).getBasePotionData().getType() == PotionType.WATER) {
                ThermalPlayer t = playerMap.get(event.getPlayer().getDisplayName());
                t.hydrate(ThermalConfig.water_bottle_hydration);
                event.getPlayer().sendMessage(t.hydrationBar());
            }
        }
    }

    @EventHandler
    public void playerWorking(BlockBreakEvent event) {
        ThermalPlayer t = getThermalPlayer(event.getPlayer().getDisplayName());
        t.work(event.getBlock().getType().getHardness());
    }

    public void updatePlayers(Temperature temp) {
        for(Player p : ThermalExtremes.plugin.getServer().getOnlinePlayers()) {
            updatePlayer(p, temp);
        }
    }

    private void updatePlayer(Player p, Temperature temp) {


        // 1.  Get Thermal Player
        ThermalPlayer t = getThermalPlayer(p.getDisplayName());
        if (t == null) return;

        time = p.getWorld().getTime();
        boolean stormy = p.getWorld().hasStorm();
        BodilyCondition currentCondition = t.condition;

        // 2.  Player statuses

        // Clothes drying
        if(t.wateryPlace) {
            t.wetness=ThermalConfig.max_wetness;
        } else if(t.outsidePlace&&stormy) {
            t.wetness+=12;
        } else if(t.wetness>0) {
            double drying = 2;
            if(t.warmedPlace) drying*=(t.standingOn.getLightFromBlocks()-ThermalConfig.block_light_heated)^2; //  Stand by the fire to dry your clothes.
            if(temp.equals(COLD)) drying -=1;
            if(temp.equals(Temperature.HOT)&&t.sunlitPlace) drying+=2;
            t.wetness -= Math.floor(drying);
            if(t.wetness<=0) {
                t.wetness=0;
                p.sendMessage("Your clothes have dried off.");
            }
        }

        // 3.  CALCULATE WEATHER EXPOSURE

        boolean currentlyExposed = t.exposed;
        String because = "You're just unlucky.";
        double exposure = 0;
        if(!t.ventilatedPlace) {
            exposure+=0.05;
            because = "This place is poorly ventilated; it is difficult to breathe.";
        }

        switch (temp) {
            case HOT:
                double radiantHeat = getRadiantHeat(p.getLocation());
                if (t.sunlitPlace) {
                    if(time<11000) {
                        because = "You are exposed to the sun's blazing wrath!";
                        exposure+=2;
                        if(t.outsidePlace) exposure++;
                    }
                    else {
                        because = "It's going to be a hot night tonight...";
                    }
                    exposure += radiantHeat;  // Radiant heat > 0
                }
                if(stormy) exposure/=4;
                break;
            case COLD:
                // Wet
                if(t.wateryPlace) {
                    exposure=5;
                    because = "Swimming?  In a cold snap?  Bad idea.";
                    break;
                }
                if(t.sunlitPlace) {
                    exposure+=t.standingOn.getLightFromSky()-ThermalConfig.sky_light_sunny;
                    because = "It's cold out here!";
                }
                if(t.wetness>0.1*ThermalConfig.max_wetness) {
                    exposure *= 1+((2*((double) t.wetness)) / ThermalConfig.max_wetness);
                    because = "Your wet clothes put you at severe risk during a cold snap.";
                }
                if(time>13000) {
                    because = "You're pretty exposed here as the temperatures start to drop.";
                    exposure*=3;
                }
                break;
            default:
                break;
        }

        if(t.debugging&&exposure>0) p.sendMessage(temp+"Current exposure is "+exposure+":"+because);

        t.exposed = exposure>0;

        if(t.exposed!=currentlyExposed) {
            if(t.exposed) p.sendMessage(because);
            else p.sendMessage("You've found a sheltered, well-ventilated place where you can recover.");
        }


        //  5. Change body temperature

        if(t.exposed) t.expose(exposure, temp);


        //  6.  Bodily conditions
        BodilyCondition newBod = t.updateBodilyCondition();
        if(!currentCondition.equals(newBod)&&newBod.severity>1) {
            if (currentCondition.severity < newBod.severity) {
                p.sendMessage("Warning!  Because of " + temp.cause + ", you are now " + t.condition.color + t.condition.effectName + ChatColor.WHITE + ".");
                p.sendMessage("Try "+t.condition.remedy+"!");
            } else {
                if(currentCondition.severity>0)
                p.sendMessage(ChatColor.AQUA+"You're recovering somewhat.  " + t.condition.color + "You're " + t.condition.effectName + " now.");
                else p.sendMessage(ChatColor.AQUA+"You've recovered!");
            }
        }

        if(t.condition.severity>2) {
            if(t.condition.risk.equals(COLD)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, t.condition.severity-1));
            }
            else if(t.condition.risk.equals(HOT)) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,200,t.condition.severity-1));
            }
        }

        // 7.   Player's body temperature regulates, assuming they're still alive.
        if(p.getHealth()>0) t.regulate();


    }

    private double getRadiantHeat(Location l) {
        BlockVector3 middle = BukkitAdapter.asBlockVector(l);
        BukkitWorld world = new BukkitWorld(l.getWorld());
        Vector2 radius = Vector2.UNIT_X.multiply(10);
        CylinderRegion region = new CylinderRegion(world, middle, radius, l.getBlockY()-2, l.getBlockY()+2);
        int x = 0;
        for(Iterator i = region.iterator(); i.hasNext();) {
            x++;
            BlockVector3 blv = (BlockVector3) i.next();
            Block block = l.getWorld().getBlockAt(blv.getBlockX(),blv.getBlockY(),blv.getBlockZ());
            ThermalExtremes.logger.log(Level.INFO, x+": "+block.getType().toString());
        }
        ThermalExtremes.logger.log(Level.INFO, "Contents "+x+", volume "+region.getVolume());
        return 0;
    }

}
