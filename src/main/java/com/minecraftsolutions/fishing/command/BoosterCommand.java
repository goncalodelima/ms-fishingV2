package com.minecraftsolutions.fishing.command;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.controller.booster.BoosterController;
import com.minecraftsolutions.fishing.model.booster.Booster;
import com.minecraftsolutions.fishing.model.booster.service.BoosterFoundationService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Optional;

public class BoosterCommand implements CommandExecutor {

    private final FileConfiguration config = FishingPlugin.getInstance().getMessages().getConfig();
    private final BoosterFoundationService boosterService = FishingPlugin.getInstance().getBoosterService();
    private final BoosterController controller = FishingPlugin.getInstance().getBoosterController();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!sender.hasPermission("booster.admin")){
            sender.sendMessage(config.getString("no-permission").replace("&", "§"));
            return false;
        }

        if (args.length != 4 || !args[0].equalsIgnoreCase("give")){
            sender.sendMessage(config.getString("booster-syntax-error").replace("&", "§"));
            return false;
        }

        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null || !target.isOnline()){
            sender.sendMessage(config.getString("invalid-player").replace("&", "§"));
            return false;
        }

        Optional<Booster> boosterOptional = boosterService.get(args[2]);

        if (!boosterOptional.isPresent()){
            sender.sendMessage(config.getString("invalid-booster").replace("&", "§"));
            return false;
        }

        Booster booster = boosterOptional.get();
        int amount;

        try {
            amount = Integer.parseInt(args[3]);
        }catch (Exception e){
            sender.sendMessage(config.getString("invalid-amount").replace("&", "§"));
            return false;
        }

        controller.giveBooster(target, booster, amount);
        sender.sendMessage(config.getString("booster-send").replace("&", "§")
                .replace("{player}", target.getName()).replace("{amount}", String.valueOf(amount)));
        return true;
    }

}
