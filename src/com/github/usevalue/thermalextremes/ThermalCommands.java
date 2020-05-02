package com.github.usevalue.thermalextremes;

import com.github.usevalue.thermalextremes.thermalcreature.ThermalPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class ThermalCommands implements CommandExecutor {

    public ThermalCommands() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(args.length==0) {
            sender.sendMessage(ChatColor.AQUA + "WORLD STATS:");
            sender.sendMessage(ChatColor.AQUA + "World temperatures are " + ThermalExtremes.clock.checkTemp());
            sender.sendMessage("Type /thermal help for commands info.");
            return true;
        }

        if(args[0].toLowerCase().equals("status")) {
            Player p;
            if(args.length==1) {
                if(sender instanceof ConsoleCommandSender) {
                    sender.sendMessage("Please specify a player from console, i.e. thermal status [name]");
                    return true;
                }
                else p = (ThermalExtremes.plugin.getServer().getPlayer(sender.getName()));
            }
            else {
                p = ThermalExtremes.plugin.getServer().getPlayer(args[1]);
                if(p==null) {
                    sender.sendMessage("Player "+args[1]+" not found.");
                    return true;
                }
            }
            ThermalPlayer t = ThermalExtremes.playerHandler.getThermalPlayer(p);
            sender.sendMessage(ChatColor.AQUA + "PLAYER STATS:");
            sender.sendMessage(ChatColor.AQUA + "Core body temperature: " + ChatColor.WHITE + Math.floor(t.getTemp() * 100) / 100 + "°C");
            sender.sendMessage(ChatColor.AQUA+"Wetness: "+ChatColor.WHITE+t.wetness);
            sender.sendMessage(ChatColor.AQUA+"Hydration: "+ChatColor.WHITE+t.hydrationBar());
            return true;
        }

        if(args[0].toLowerCase().equals("help")) {
            sender.sendMessage(ChatColor.AQUA+"ThermalExtremes commands:");
            sender.sendMessage(ChatColor.GRAY+"status, list, heatwave, coldsnap, normalise, toggle");
            return true;
        }

        if(args[0].toLowerCase().equals("list")) {
            sender.sendMessage("Currently tracking the following players:");
            for(Player p : ThermalExtremes.playerHandler.getThermalPlayers().keySet()) {
                sender.sendMessage(p.getDisplayName()+" has a body temperature of "+ThermalExtremes.playerHandler.getThermalPlayer(p).getTemp()+"°C.");
            }
            return true;
        }

        if(args[0].toLowerCase().equals("toggle")) {
            if(args.length==1) {
                sender.sendMessage("You can toggle [random] weather, or console-side [debug] mode.");
                return true;
            }
            switch(args[1].toLowerCase()) {
                case "random":
                    ThermalExtremes.clock.randomWeather = !ThermalExtremes.clock.randomWeather;
                    String s = "disabled";
                    if (ThermalExtremes.clock.randomWeather) s = "enabled";
                    sender.sendMessage("Random weather changes are now " + s + ".");
                    return true;
                case "debug":
                    ThermalExtremes.debugMode = !ThermalExtremes.debugMode;
                    sender.sendMessage("Toggled console debug mode.");
                    return true;
                default:
                    sender.sendMessage("You can only toggle 'random' and 'debug' I'm afraid.");
                    return true;
            }
        }

        if(args[0].toLowerCase().equals("heatwave")) {
            if(ThermalExtremes.clock.beginHeatwave()) sender.sendMessage("Starting a heatwave!");
            else sender.sendMessage("There's already a heatwave!");
            return true;
        }

        if(args[0].toLowerCase().equals("coldsnap")) {
            ThermalExtremes.clock.beginColdSnap();
            return true;
        }

        if(args[0].toLowerCase().equals("normalise")) {
            ThermalExtremes.clock.normaliseTemps();
            return true;
        }

        return false;
    }
}
