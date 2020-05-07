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
            if(!sender.hasPermission("thermalextremes.user")) {
                sender.sendMessage("Sorry, you don't have that permission!");
                return true;
            }
            ThermalPlayer t;
            if (args.length == 0) {
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage("You need to supply the name of an online player to use this command from the console.");
                    return false;
                } else t = ThermalExtremes.playerHandler.getThermalPlayer(((Player) sender).getDisplayName());
            } else if (args.length == 1) {
                if(!sender.hasPermission("thermalextremes.other")) {
                    sender.sendMessage("Sorry, you don't have permission to get other players' stats!");
                    return true;
                }
                t = ThermalExtremes.playerHandler.getThermalPlayer(args[0]);
            } else return false;
            bodyInfo(sender, t);
            return true;
        }

        if (label.equals("thermal")) {

            if (args.length == 0) {
                sender.sendMessage(ChatColor.AQUA + "World temperatures are " + ThermalExtremes.clock.checkTemp());
                if(sender.hasPermission("thermalextremes.admin")) sender.sendMessage("Type /thermal help for commands info.");
                return true;
            }
            if(!sender.hasPermission("thermalextremes.admin")) {
                sender.sendMessage("That's an administrative command!  Use /body instead.");
                return true;
            }
            switch (args[0]) {
                case "help":
                    sender.sendMessage(ChatColor.AQUA + "ThermalExtremes commands:");
                    sender.sendMessage(ChatColor.GRAY + "list, heatwave, coldsnap, normalise, random, debug");
                    return true;
                case "list":
                    sender.sendMessage("Currently tracking the following players:");
                    for (String name : ThermalExtremes.playerHandler.getThermalPlayers().keySet()) {
                        sender.sendMessage(name + " has a body temperature of "
                                + ThermalExtremes.playerHandler.getThermalPlayer(name).getTemp() + "°C.");
                    }
                    return true;
                case "random":
                    ThermalExtremes.clock.randomWeather = !ThermalExtremes.clock.randomWeather;
                    String s = "disabled";
                    if (ThermalExtremes.clock.randomWeather) s = "enabled";
                    sender.sendMessage("Random weather changes are now " + s + ".");
                    return true;
                case "debug":
                    if(sender instanceof ConsoleCommandSender) {
                        ThermalExtremes.debugMode = !ThermalExtremes.debugMode;
                        sender.sendMessage("Toggled console debug mode.");
                    }
                    else {
                        ThermalExtremes.playerHandler.getThermalPlayer(((Player) sender).getDisplayName()).debugging = !ThermalExtremes.playerHandler.getThermalPlayer(((Player) sender).getDisplayName()).debugging;
                        sender.sendMessage("Player debug mode: "+ThermalExtremes.playerHandler.getThermalPlayer(((Player) sender).getDisplayName()).debugging);
                    }
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
                case "reload":
                    ThermalExtremes.plugin.reloadTheConfigs();
                    return true;
                default:
                    return false;
            }
        }

        return false;
    }

    private void bodyInfo(CommandSender recipient, ThermalPlayer t) {
        recipient.sendMessage(ChatColor.AQUA + "PLAYER STATS:");
        recipient.sendMessage(ChatColor.AQUA + "Core body temperature: " + ChatColor.WHITE + Math.floor(t.getTemp() * 100) / 100 + "°C ("+t.condition+")");
        recipient.sendMessage(ChatColor.AQUA+"Clothing: "+t.getWetnessDescription());
        recipient.sendMessage(ChatColor.AQUA+"Hydration: "+ChatColor.WHITE+t.hydrationBar());
    }
}
