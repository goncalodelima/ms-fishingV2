package com.minecraftsolutions.fishing.view;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.model.fishingrod.FishingRod;
import com.minecraftsolutions.fishing.model.fishingrod.service.FishingRodFoundationService;
import com.minecraftsolutions.fishing.model.user.User;
import com.minecraftsolutions.fishing.model.user.service.UserFoundationService;
import com.minecraftsolutions.fishing.util.item.ItemBuilder;
import me.devnatan.inventoryframework.View;
import me.devnatan.inventoryframework.ViewConfigBuilder;
import me.devnatan.inventoryframework.component.Pagination;
import me.devnatan.inventoryframework.context.Context;
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

public class FishingRodManagerView extends View {

    private final FileConfiguration messages = FishingPlugin.getInstance().getMessages().getConfig();
    private final FileConfiguration config = FishingPlugin.getInstance().getFishingRodManager().getConfig();
    private final UserFoundationService userService = FishingPlugin.getInstance().getUserService();
    private final FishingRodFoundationService fishingRodService = FishingPlugin.getInstance().getFishingRodService();

    private final State<ItemStack> buySlotState = computedState(render -> {

        final Player player = render.getPlayer();
        final Optional<User> userOptional = userService.get(player.getName());

        if (!userOptional.isPresent())
            throw new RuntimeException();

        final User user = userOptional.get();
        final FishingRod fishingRod = user.getFishingRod();

        if (fishingRod.hasSlotToBuy()){

            boolean hasFounds = FishingRod.SLOT_CURRENCY.get(player) >= FishingRod.SLOT_PRICE;
            String status = hasFounds ? config.getString("upgrade.status.has-founds") : config.getString("upgrade.status.no-founds");

            return new ItemBuilder(Material.getMaterial(config.getString("upgrade.has-upgrade.material")), 1, (short) config.getInt("upgrade.has-upgrade.data"))
                    .setSkull(config.getString("upgrade.has-upgrade.url"))
                    .setDisplayName(config.getString("upgrade.has-upgrade.name"))
                    .setLore(config.getStringList("upgrade.has-upgrade.lore").stream().map(str -> str.replace("{icon}", FishingRod.SLOT_CURRENCY.getIcon())
                                    .replace("{price}", FishingPlugin.FORMATTER.formatNumber(FishingRod.SLOT_PRICE)))
                            .collect(Collectors.toList()))
                    .updateLore("{status}", status)
                    .glow(config.getBoolean("upgrade.has-upgrade.glow"))
                    .build();

        }else{

            return new ItemBuilder(Material.getMaterial(config.getString("upgrade.no-upgrade.material")), 1, (short) config.getInt("upgrade.no-upgrade.data"))
                    .setSkull(config.getString("upgrade.no-upgrade.url"))
                    .setDisplayName(config.getString("upgrade.no-upgrade.name"))
                    .setLore(config.getStringList("upgrade.no-upgrade.lore"))
                    .glow(config.getBoolean("upgrade.no-upgrade.glow"))
                    .build();

        }

    });

    private final State<Pagination> paginationState = buildComputedPaginationState(context -> {

        final Player player = context.getPlayer();
        final Optional<User> userOptional = userService.get(player.getName());

        if (!userOptional.isPresent())
            return Collections.emptyList();

        final User user = userOptional.get();
        final FishingRod fishingRod = user.getFishingRod();
        return new ArrayList<>(fishingRod.getSlots());
    })
            .layoutTarget('S')
            .elementFactory((context, bukkitItemComponentBuilder, i, slot) -> {

                ItemStack item;

                if (slot.isBought()){

                    if (slot.hasEnchant()){

                        item = new ItemBuilder(slot.getEnchant().getMaterial(), 1, slot.getEnchant().getData())
                                .setSkull(slot.getEnchant().getUrl())
                                .setDisplayName(config.getString("slot.has-enchant.name").replace("{pos}", String.valueOf(slot.getPosition())))
                                .setLore(config.getStringList("slot.has-enchant.lore")
                                        .stream().map(str -> str.replace("{name}", slot.getEnchant().getName()))
                                        .collect(Collectors.toList()))
                                .glow(config.getBoolean("slot.has-enchant.glow"))
                                .build();

                    }else{

                        item = new ItemBuilder(Material.getMaterial(config.getString("slot.no-enchant.material")), 1, (short) config.getInt("slot.no-enchant.data"))
                                .setSkull(config.getString("slot.no-enchant.url"))
                                .setDisplayName(config.getString("slot.no-enchant.name").replace("{pos}", String.valueOf(slot.getPosition())))
                                .setLore(config.getStringList("slot.no-enchant.lore"))
                                .glow(config.getBoolean("slot.no-enchant.glow"))
                                .build();

                    }

                }else{

                    item = new ItemBuilder(Material.getMaterial(config.getString("slot.lock.material")), 1, (short) config.getInt("slot.lock.data"))
                            .setSkull(config.getString("slot.lock.url"))
                            .setDisplayName(config.getString("slot.lock.name").replace("{pos}", String.valueOf(slot.getPosition())))
                            .setLore(config.getStringList("slot.lock.lore"))
                            .glow(config.getBoolean("slot.lock.glow"))
                            .build();

                }

                bukkitItemComponentBuilder.withItem(item)
                        .onClick(click -> {

                            final Player player = click.getPlayer();

                            if (slot.isBought())
                                FishingPlugin.getInstance().getViewFrame().open(FishingRodSlotView.class, player, slot.getPosition());

                        });
            })
            .build();

    @Override
    public void onInit(@NotNull ViewConfigBuilder config) {

        config
                .title(this.config.getString("title").replace("&", "§"))
                .size(this.config.getInt("size"))
                .layout(this.config.getStringList("layout").toArray(new String[0]))
                .cancelOnClick()
                .cancelOnDrag()
                .cancelOnDrop()
                .cancelOnPickup();

    }

    @Override
    public void onFirstRender(@NotNull RenderContext render) {


        ItemStack enchant = new ItemBuilder(Material.getMaterial(config.getString("enchant.material")), 1, (short) config.getInt("enchant.data"))
                .setSkull(config.getString("enchant.url"))
                .setDisplayName(config.getString("enchant.name"))
                .setLore(config.getStringList("enchant.lore"))
                .glow(config.getBoolean("enchant.glow"))
                .build();

        render.layoutSlot('E', enchant)
                .onClick(click -> {

                    Player player = click.getPlayer();
                    click.closeForPlayer();
                    FishingPlugin.getInstance().getViewFrame().open(FishingRodEnchantsView.class, player);

                });

        ItemStack back = new ItemBuilder(Material.getMaterial(config.getString("back.material")), 1, (short) config.getInt("back.data"))
                .setSkull(config.getString("back.url"))
                .setDisplayName(config.getString("back.name"))
                .setLore(config.getStringList("back.lore"))
                .glow(config.getBoolean("back.glow"))
                .build();

        render.layoutSlot('B', back)
                .onClick(click -> {

                    Player clickPlayer = click.getPlayer();
                    click.closeForPlayer();
                    FishingPlugin.getInstance().getViewFrame().open(FishingExitView.class, clickPlayer);

                });

        render
                .layoutSlot('U')
                .watch(buySlotState)
                .renderWith(() -> buySlotState.get(render))
                .onClick(click -> {
                    final Player clickPlayer = click.getPlayer();
                    Optional<User> clickUserOptional = userService.get(clickPlayer.getName());

                    if (!clickUserOptional.isPresent()){
                        click.closeForPlayer();
                        return;
                    }

                    User clickUser = clickUserOptional.get();
                    FishingRod clickFishingRod = clickUser.getFishingRod();

                    if (!clickFishingRod.hasSlotToBuy()){
                        clickPlayer.sendMessage(messages.getString("no-slot-to-buy").replace("&", "§"));
                        return;
                    }

                    boolean clickHasFounds = FishingRod.SLOT_CURRENCY.get(clickPlayer) >= FishingRod.SLOT_PRICE;

                    if (!clickHasFounds){
                        clickPlayer.sendMessage(messages.getString("buy-slot-no-founds").replace("&", "§"));
                        return;
                    }

                    FishingRod.SLOT_CURRENCY.remove(clickPlayer, FishingRod.SLOT_PRICE);
                    clickFishingRod.getNextSlot().setBought(true);
                    fishingRodService.update(clickFishingRod);
                    click.update();
                    paginationState.get(render).update();
                });

    }

    @Override
    public void onUpdate(@NotNull Context update) {
        paginationState.get(update).forceUpdate();
    }

}
