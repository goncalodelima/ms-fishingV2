package com.minecraftsolutions.fishing.hook;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.model.user.User;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class PlaceholderAPIHook extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "ms-fishingV2";
    }

    @Override
    public @NotNull String getAuthor() {
        return "yDioguin_";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {

        Optional<User> user = FishingPlugin.getInstance().getUserService().get(player.getName());

        if (!user.isPresent())
            return "Relogue";

        switch (params.toLowerCase()) {
            case "fishers":
                return String.valueOf(FishingPlugin.getInstance().getUserController().getFishers());
            case "isempty":
                return user.get().getBucket().getCapacity() == user.get().getBucket().getMaxCapacity() ? "§c§l✔" : "§a§l✖";
            default:
                return "Placeholder inválida";
        }

    }

}