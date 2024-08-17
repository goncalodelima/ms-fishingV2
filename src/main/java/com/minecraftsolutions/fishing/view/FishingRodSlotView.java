package com.minecraftsolutions.fishing.view;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.controller.user.UserController;
import com.minecraftsolutions.fishing.model.enchant.service.EnchantFoundationService;
import com.minecraftsolutions.fishing.model.fishingrod.FishingRod;
import com.minecraftsolutions.fishing.model.fishingrod.service.FishingRodFoundationService;
import com.minecraftsolutions.fishing.model.fishingrod.slot.Slot;
import com.minecraftsolutions.fishing.model.user.User;
import com.minecraftsolutions.fishing.model.user.service.UserFoundationService;
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

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public class FishingRodSlotView extends View {

    private final FileConfiguration config = FishingPlugin.getInstance().getFishingRodSlot().getConfig();
    private final EnchantFoundationService enchantService = FishingPlugin.getInstance().getEnchantService();
    private final UserFoundationService userService = FishingPlugin.getInstance().getUserService();
    private final FishingRodFoundationService fishingRodService = FishingPlugin.getInstance().getFishingRodService();
    private final UserController userController = FishingPlugin.getInstance().getUserController();

    private final State<Pagination> paginationState = buildLazyPaginationState(context -> {

        final Player player = context.getPlayer();
        final Optional<User> userOptional = userService.get(player.getName());

        if (!userOptional.isPresent())
            return Collections.emptyList();

        User user = userOptional.get();
        FishingRod fishingRod = user.getFishingRod();

        return enchantService.getAll()
                .stream()
                .filter(enchant -> fishingRod.getEnchantLevel(enchant) > 0)
                .collect(Collectors.toList());
    })
            .layoutTarget('E')
            .itemFactory((bukkitItemComponentBuilder, enchant) -> {

                bukkitItemComponentBuilder.onRender(context -> {

                    Player player = context.getPlayer();
                    Optional<User> userOptional = userService.get(player.getName());

                    if (!userOptional.isPresent())
                        return;

                    User user = userOptional.get();
                    FishingRod fishingRod = user.getFishingRod();
                    Slot slot = fishingRod.getSlot((Integer) context.getInitialData());
                    String status;

                    if (slot.hasEnchant() && slot.getEnchant().equals(enchant))
                        status = config.getString("enchant.status.remove");
                    else if (fishingRod.hasEnchantApplied(enchant))
                        status = config.getString("enchant.status.in-use").replace("{pos}", String.valueOf(fishingRod.getSlotEnchant(enchant).getPosition()));
                    else
                        status = config.getString("enchant.status.no-use");

                    context.setItem(new ItemBuilder(enchant.getMaterial(), 1, enchant.getData())
                            .setSkull(enchant.getUrl())
                            .setDisplayName(config.getString("enchant.name").replace("{name}", enchant.getName()))
                            .setLore(config.getStringList("enchant.lore").stream()
                                    .map(str -> str.replace("{level}", FishingPlugin.FORMATTER.formatNumber(fishingRod.getEnchantLevel(enchant)))
                                            .replace("{status}", status.replace("&", "§")))
                                    .collect(Collectors.toList()))
                            .updateLore("{description}", enchant.getDescription())
                            .glow(config.getBoolean("enchant.glow"))
                            .build());

                }).onClick(click -> {

                    Player player = click.getPlayer();
                    Optional<User> userOptional = userService.get(player.getName());

                    if (!userOptional.isPresent())
                        return;

                    User user = userOptional.get();
                    FishingRod fishingRod = user.getFishingRod();
                    Slot slot = fishingRod.getSlot((Integer) click.getInitialData());

                    if (slot.hasEnchant() && fishingRod.hasEnchantApplied(enchant) && fishingRod.getSlotEnchant(enchant).equals(slot)){
                        slot.setEnchant(null);
                        fishingRodService.update(fishingRod);
                        userController.setItens(user);
                        click.update();
                        return;
                    }

                    if (!slot.hasEnchant() && !fishingRod.hasEnchantApplied(enchant)){
                        slot.setEnchant(enchant);
                        fishingRodService.update(fishingRod);
                        userController.setItens(user);
                        click.update();
                    }

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
                    FishingPlugin.getInstance().getViewFrame().open(FishingRodManagerView.class, clickPlayer);

                });

    }

}
