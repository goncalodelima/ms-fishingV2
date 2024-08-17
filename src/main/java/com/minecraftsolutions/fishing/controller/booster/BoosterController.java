package com.minecraftsolutions.fishing.controller.booster;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.model.booster.Booster;
import com.minecraftsolutions.fishing.model.booster.service.BoosterFoundationService;
import com.minecraftsolutions.fishing.util.BukkitUtils;
import com.minecraftsolutions.fishing.util.item.ItemBuilder;
import com.minecraftsolutions.fishing.util.item.ItemNBT;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.stream.Collectors;

public class BoosterController {

    private final BoosterFoundationService boosterService = FishingPlugin.getInstance().getBoosterService();

    public Optional<Booster> getByItem(ItemStack item){

        if (ItemNBT.hasTag(item, "booster-id"))
            return boosterService.get(ItemNBT.getString(item, "booster-id"));

        return Optional.empty();
    }

    public int getBoosterTime(ItemStack item){

        if (ItemNBT.hasTag(item, "booster-time"))
            return ItemNBT.getInt(item, "booster-time");

        return 0;
    }

    public void giveBooster(Player player, Booster booster, int amount){
        giveBooster(player, booster, booster.getDefaultTime(), amount);
    }

    public void giveBooster(Player player, Booster booster, int time, int amount){

        ItemStack item = new ItemBuilder(booster.getDisplay().clone())
                .changeItemMeta(meta -> meta.setLore(meta.getLore().stream().map(str -> str.replace("{multiplier}", String.format("%.2f", booster.getMultiplier()))
                        .replace("{time}", BukkitUtils.formatSeconds(time)))
                        .collect(Collectors.toList())))
                .build();

        item.setAmount(amount);
        ItemNBT.setString(item, "booster-id", booster.getId());
        ItemNBT.setInt(item, "booster-time", time);
        player.getInventory().addItem(item);
    }

}
