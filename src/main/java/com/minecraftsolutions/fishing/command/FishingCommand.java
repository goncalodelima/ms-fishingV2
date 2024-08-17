package com.minecraftsolutions.fishing.command;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.model.user.User;
import com.minecraftsolutions.fishing.model.user.fishing.FishingType;
import com.minecraftsolutions.fishing.model.user.service.UserFoundationService;
import com.minecraftsolutions.fishing.util.LocationSerializer;
import com.minecraftsolutions.fishing.util.configuration.Configuration;
import com.minecraftsolutions.fishing.view.FishingExitView;
import com.minecraftsolutions.fishing.view.FishingJoinView;
import me.devnatan.inventoryframework.ViewFrame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Optional;

public class FishingCommand implements CommandExecutor {

    private final Configuration locations = FishingPlugin.getInstance().getLocations();
    private final FileConfiguration config = FishingPlugin.getInstance().getMessages().getConfig();
    private final UserFoundationService userService = FishingPlugin.getInstance().getUserService();
    private final ViewFrame viewFrame = FishingPlugin.getInstance().getViewFrame();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        Optional<User> userOptional = userService.get(player.getName());

        if (!userOptional.isPresent())
            return false;

        User user = userOptional.get();

        if (args.length == 0 || !player.hasPermission("fishing.admin")){
            if (user.getFishingType().equals(FishingType.IN))
                viewFrame.open(FishingExitView.class, player);
            else
                viewFrame.open(FishingJoinView.class, player);
            return true;
        }

        if (args[0].equalsIgnoreCase("setspawn")){
            locations.getConfig().set("spawn", LocationSerializer.serializeLocation(player.getLocation()));
            locations.saveConfig();
            player.sendMessage(config.getString("set-spawn").replace("&", "§"));
            return true;
        }

        if (args[0].equalsIgnoreCase("setexit")){
            locations.getConfig().set("exit", LocationSerializer.serializeLocation(player.getLocation()));
            locations.saveConfig();
            player.sendMessage(config.getString("set-exit").replace("&", "§"));
            return true;
        }

        config.getStringList("help")
                .stream()
                .map(str -> str.replace("&", "§"))
                .forEach(player::sendMessage);
        return false;
    }
}
