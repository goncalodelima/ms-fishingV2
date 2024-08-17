package com.minecraftsolutions.fishing.view;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.model.bucket.Bucket;
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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public class FishingExitView extends View {

    private final FileConfiguration config = FishingPlugin.getInstance().getFishingExit().getConfig();
    private final UserFoundationService userService = FishingPlugin.getInstance().getUserService();

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
                ItemStack item = new ItemBuilder(booster.getDisplay().clone())
                        .changeItemMeta(meta -> meta.setLore(meta.getLore().stream().map(str -> str.replace("{multiplier}", String.format("%.2f", booster.getMultiplier()))
                                        .replace("{time}", BukkitUtils.formatSeconds(user.getBoosters().getOrDefault(booster, 0) / 20)))
                                .collect(Collectors.toList())))
                        .build();

                bukkitItemComponentBuilder.withItem(item);
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

        Player player = render.getPlayer();
        Optional<User> userOptional = userService.get(player.getName());

        if (!userOptional.isPresent()) {
            render.closeForPlayer();
            return;
        }

        User user = userOptional.get();
        Bucket bucket = user.getBucket();

        ItemStack bucketItem = new ItemBuilder(Material.getMaterial(config.getString("bucket.material")), 1, (short) config.getInt("bucket.data"))
                .setSkull(config.getString("bucket.url"))
                .setDisplayName(config.getString("bucket.name"))
                .setLore(config.getStringList("bucket.lore").stream()
                        .map(str -> str.replace("{progress_bar}", bucket.getProgressBar()).replace("{progress_raw}", String.format("%.2f", bucket.getProgressRaw() * 100))
                                .replace("{amount}", FishingPlugin.FORMATTER.formatNumber(bucket.getCapacity())))
                        .collect(Collectors.toList()))
                .build();

        render.layoutSlot('U', bucketItem)
                .onClick(click -> {

                    Player clickPlayer = click.getPlayer();
                    click.closeForPlayer();
                    FishingPlugin.getInstance().getViewFrame().open(BucketView.class, clickPlayer);

                });

        ItemStack fishingRod = new ItemBuilder(Material.getMaterial(config.getString("fishingrod.material")), 1, (short) config.getInt("fishingrod.data"))
                .setSkull(config.getString("fishingrod.url"))
                .setDisplayName(config.getString("fishingrod.name"))
                .setLore(config.getStringList("fishingrod.lore").stream()
                        .map(str -> str.replace("{progress_bar}", bucket.getProgressBar()).replace("{progress_raw}", String.format("%.2f", bucket.getProgressRaw() * 100))
                                .replace("{amount}", FishingPlugin.FORMATTER.formatNumber(bucket.getCapacity())))
                        .collect(Collectors.toList()))
                .glow(config.getBoolean("fishingrod.glow"))
                .build();

        render.layoutSlot('F', fishingRod)
                .onClick(click -> {

                    Player clickPlayer = click.getPlayer();
                    click.closeForPlayer();
                    FishingPlugin.getInstance().getViewFrame().open(FishingRodManagerView.class, clickPlayer);

                });

        ItemStack ranking = new ItemBuilder(Material.getMaterial(config.getString("ranking.material")), 1, (short) config.getInt("ranking.data"))
                .setSkull(config.getString("ranking.url"))
                .setDisplayName(config.getString("ranking.name"))
                .setLore(config.getStringList("ranking.lore").stream()
                        .map(str -> str.replace("{progress_bar}", bucket.getProgressBar()).replace("{progress_raw}", String.format("%.2f", bucket.getProgressRaw() * 100))
                                .replace("{amount}", FishingPlugin.FORMATTER.formatNumber(bucket.getCapacity())))
                        .collect(Collectors.toList()))
                .glow(config.getBoolean("ranking.glow"))
                .build();

        render.layoutSlot('R', ranking)
                .onClick(click -> {

                    Player clickPlayer = click.getPlayer();
                    click.closeForPlayer();
                    FishingPlugin.getInstance().getViewFrame().open(RankingView.class, clickPlayer);

                });

    }

}
