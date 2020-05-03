package com.github.usevalue.thermalextremes;

import com.github.usevalue.thermalextremes.thermalcreature.ThermalPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class ThermalCommands implements CommandExecutor {

    public ThermalCommands() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (label.equals("body")) {
            ThermalPlayer t;
            if (args.length == 0) {
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage("You need to supply the name of an online player to use this command from the console.");
                    return false;
                } else t = ThermalExtremes.playerHandler.getThermalPlayer((Player) sender);
            } else if (args.length == 1) {
                Player p = ThermalExtremes.plugin.getServer().getPlayer(args[0]);
                t = ThermalExtremes.playerHandler.getThermalPlayer(p);
            } else return false;
            bodyInfo(sender, t);
            return true;
        }

        if (label.equals("thermal")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.AQUA + "WORLD STATS:");
                sender.sendMessage(ChatColor.AQUA + "World temperatures are " + ThermalExtremes.clock.checkTemp());
                sender.sendMessage("Type /thermal help for commands info.");
                return true;
            } else switch (args[0]) {
                case "help":
                    sender.sendMessage(ChatColor.AQUA + "ThermalExtremes commands:");
                    sender.sendMessage(ChatColor.GRAY + "list, heatwave, coldsnap, normalise, random, debug");
                    return true;
                case "list":
                    sender.sendMessage("Currently tracking the following players:");
                    for (Player p : ThermalExtremes.playerHandler.getThermalPlayers().keySet()) {
                        sender.sendMessage(p.getDisplayName() + " has a body temperature of "
                                + ThermalExtremes.playerHandler.getThermalPlayer(p).getTemp() + "°C.");
                    }
                    return true;
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
                case "heatwave":
                    if (ThermalExtremes.clock.beginHeatwave()) sender.sendMessage("Starting a heatwave!");
                    else sender.sendMessage("There's already a heatwave!");
                    return true;
                case "coldsnap":
                    ThermalExtremes.clock.beginColdSnap();
                    return true;
                case "normalise":
                    ThermalExtremes.clock.normaliseTemps();
                    return true;
                default:
                    return false;
            }
        }

        return false;
    }

    private void bodyInfo(CommandSender recipient, ThermalPlayer t) {
        recipient.sendMessage(ChatColor.AQUA + "PLAYER STATS:");
        recipient.sendMessage(ChatColor.AQUA + "Core body temperature: " + ChatColor.WHITE + Math.floor(t.getTemp() * 100) / 100 + "°C");
        recipient.sendMessage(ChatColor.AQUA+"Clothing: "+t.getWetnessDescription());
        recipient.sendMessage(ChatColor.AQUA+"Hydration: "+ChatColor.WHITE+t.hydrationBar());
    }
}
