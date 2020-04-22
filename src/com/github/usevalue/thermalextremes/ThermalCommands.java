package com.github.usevalue.thermalextremes;

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
            if(sender instanceof ConsoleCommandSender) {
                sender.sendMessage("Possible ThermalExtremes console commands:");
                sender.sendMessage("status, list, heatwave, coldsnap, normalise.  Type 'thermal help [command]' for a description of what it does.");
            }
            else {
                Player player = ThermalExtremes.plugin.getServer().getPlayer(sender.getName());
                ThermalPlayer p = ThermalExtremes.playerHandler.getThermalPlayer(player);
                sender.sendMessage("You currently have an internal body temperature of "+ p.getTemp()
                        +"°C.  Good!");
                sender.sendMessage("Overall world temperatures are "+ ThermalExtremes.clock.checkTemp() +".");
                sender.sendMessage("Type /thermal help for available subcommands.");
            }
            return true;
        }

        if(args[0].toLowerCase().equals("help")) {
            sender.sendMessage("Help!");
            return true;
        }

        if(args[0].toLowerCase().equals("list")) {
            sender.sendMessage("&BCurrently tracking the following players:");
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
        return false;
    }
}
