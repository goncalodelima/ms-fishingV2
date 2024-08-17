package com.minecraftsolutions.fishing.view;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.controller.booster.BoosterController;
import com.minecraftsolutions.fishing.controller.user.UserController;
import com.minecraftsolutions.fishing.controller.user.exception.EmptyInventoryException;
import com.minecraftsolutions.fishing.controller.user.exception.LocationException;
import com.minecraftsolutions.fishing.model.bucket.Bucket;
import com.minecraftsolutions.fishing.model.fishingrod.FishingRod;
import com.minecraftsolutions.fishing.model.user.User;
import com.minecraftsolutions.fishing.model.user.service.UserFoundationService;
import com.minecraftsolutions.fishing.util.BukkitUtils;
import com.minecraftsolutions.fishing.util.item.ItemBuilder;
import me.devnatan.inventoryframework.View;
import me.devnatan.inventoryframework.ViewConfigBuilder;
import me.devnatan.inventoryframework.component.Pagination;
import me.devnatan.inventoryframework.context.RenderContext;
import me.devnatan.inventoryframework.state.State;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public class FishingJoinView extends View {

    private final FileConfiguration messages = FishingPlugin.getInstance().getMessages().getConfig();
    private final FileConfiguration config = FishingPlugin.getInstance().getFishingJoin().getConfig();
    private final UserFoundationService userService = FishingPlugin.getInstance().getUserService();
    private final BoosterController boosterController = FishingPlugin.getInstance().getBoosterController();
    private final UserController userController = FishingPlugin.getInstance().getUserController();

    private final State<Pagination> paginationState = buildComputedPaginationState(context -> {

       final Player player = context.getPlayer();
       final Optional<User> userOptional = userService.get(player.getName());

       if (!userOptional.isPresent())
           return Collections.emptyList();

       final User user = userOptional.get();
       return new ArrayList<>(user.getBoosters().keySet());
    })
    .layoutTarget('B')
    .elementFactory((context, bukkitItemComponentBuilder, i, booster) -> {

        final Player player = context.getPlayer();
        final Optional<User> userOptional = userService.get(player.getName());

        if (!userOptional.isPresent())
            return;

        final User user = userOptional.get();
        int timeB = user.getBoosters().getOrDefault(booster, 0) / 20;

        if (timeB < 0){
            player.closeInventory();
            user.getBoosters().remove(booster);
            return;
        }

        ItemStack item = new ItemBuilder(booster.getDisplay().clone())
                .changeItemMeta(meta -> meta.setLore(meta.getLore().stream().map(str -> str.replace("{multiplier}", String.format("%.2f", booster.getMultiplier()))
                                .replace("{time}", BukkitUtils.formatSeconds(timeB)))
                        .collect(Collectors.toList())))
                .addLore(config.getStringList("booster.lore"))
                .build();

        bukkitItemComponentBuilder.withItem(item)
                .onClick(click -> {

                    final Player clickPlayer = click.getPlayer();
                    final Optional<User> clickUserOptional = userService.get(clickPlayer.getName());

                    if (!clickUserOptional.isPresent())
                        return;

                    final User clickUser = clickUserOptional.get();
                    final int time = (int) (clickUser.getBoosterTime(booster) * 0.9) / 20;
                    userService.removeBooster(user, booster);

                    if (time <= 0)
                        return;

                    boosterController.giveBooster(clickPlayer, booster, time, 1);
                    click.closeForPlayer();
                });
    })
    .build();

    @Override
    public void onInit(@NotNull ViewConfigBuilder config) {

        config
                .title(this.config.getString("title").replace("&", "§"))
                .size(this.config.getInt("size"))
                .layout(this.config.getStringList("layout").toArray(new String[0]))
                .cancelOnPickup()
                .cancelOnDrop()
                .cancelOnDrag()
                .cancelOnClick();

    }

    @Override
    public void onFirstRender(@NotNull RenderContext render) {

        ItemStack info = new ItemBuilder(Material.getMaterial(config.getString("info.material")), config.getInt("info.amount"), (short) config.getInt("info.data"))
                .setSkull(config.getString("info.url"))
                .setDisplayName(config.getString("info.name"))
                .setLore(config.getStringList("info.lore"))
                .glow(config.getBoolean("info.glow"))
                .build();

        render.layoutSlot('I', info)
                .onClick(click -> {

                    final Player player = click.getPlayer();
                    userService.get(player.getName()).ifPresent(user -> {
                        try {
                            userController.enterFishingArea(user);
                        } catch (LocationException e) {
                            click.closeForPlayer();
                            player.sendMessage(messages.getString("location-error").replace("&", "§"));
                        } catch (EmptyInventoryException e){
                            click.closeForPlayer();
                            player.sendMessage(messages.getString("empty-inventory-error").replace("&", "§")
                                    .replace("{slots}", "[" + (FishingRod.SLOT + 1) + ", " + (Bucket.SLOT + 1) + ", " + (FishingPlugin.getInstance().getConfig().getInt("exit.slot") + 1) +"]"));
                        }
                    });

                });

    }

}
