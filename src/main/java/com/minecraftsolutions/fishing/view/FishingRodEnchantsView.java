package com.minecraftsolutions.fishing.view;

import com.minecraftsolutions.economy.api.EconomyAPI;
import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.controller.user.UserController;
import com.minecraftsolutions.fishing.model.enchant.service.EnchantFoundationService;
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

import java.util.Optional;
import java.util.stream.Collectors;

public class FishingRodEnchantsView extends View {

    private final EconomyAPI api = new EconomyAPI();
    private final FileConfiguration messages = FishingPlugin.getInstance().getMessages().getConfig();
    private final FileConfiguration config = FishingPlugin.getInstance().getFishingRodEnchants().getConfig();
    private final EnchantFoundationService enchantService = FishingPlugin.getInstance().getEnchantService();
    private final UserFoundationService userService = FishingPlugin.getInstance().getUserService();
    private final FishingRodFoundationService fishingRodService = FishingPlugin.getInstance().getFishingRodService();
    private final UserController userController = FishingPlugin.getInstance().getUserController();

    private final State<Pagination> paginationState = buildLazyPaginationState(context -> enchantService.getAll())
            .layoutTarget('E')
            .itemFactory((bukkitItemComponentBuilder, enchant) -> {

                bukkitItemComponentBuilder.onRender(context -> {

                    final Player player = context.getPlayer();
                    final Optional<User> userOptional = userService.get(player.getName());
                    final Optional<com.minecraftsolutions.economy.model.user.User> economyUserOptional = api.getUser(player.getName());

                    if (!userOptional.isPresent() || !economyUserOptional.isPresent())
                        return;

                    final com.minecraftsolutions.economy.model.user.User economyUser = economyUserOptional.get();
                    final User user = userOptional.get();
                    final FishingRod fishingRod = user.getFishingRod();
                    final double price = enchant.getPrice(fishingRod.getEnchantLevel(enchant));
                    final int currentLevel = fishingRod.getEnchantLevel(enchant);

                    String status;

                    if (fishingRod.getEnchantLevel(enchant) >= enchant.getMaxLevel())
                        status = config.getString("enchant.status.max-level");
                    else if (api.get(enchant.getCurrency(), economyUser) < price)
                        status = config.getString("enchant.status.no-founds").replace("{currency}", enchant.getCurrency().getName());
                    else status = config.getString("enchant.status.has-founds");

                    context.setItem(new ItemBuilder(enchant.getMaterial(), 1, enchant.getData())
                                    .setSkull(enchant.getUrl())
                                    .setDisplayName(config.getString("enchant.name").replace("{name}", enchant.getName()).replace("{current_level}", FishingPlugin.FORMATTER.formatNumber(currentLevel))
                                            .replace("{next_level}", FishingPlugin.FORMATTER.formatNumber(currentLevel + 1)))
                                    .setLore(config.getStringList("enchant.lore").stream().map(str -> str.replace("{icon}", enchant.getCurrency().getIcon())
                                                    .replace("{price}", FishingPlugin.FORMATTER.formatNumber(price)).replace("{max_level}", FishingPlugin.FORMATTER.formatNumber(enchant.getMaxLevel()))
                                                    .replace("{status}", status.replace("&", "§")))
                                            .collect(Collectors.toList()))
                                    .updateLore("{description}", enchant.getDescription())
                                    .glow(config.getBoolean("enchant.glow"))
                                    .build());


                }).onClick(click -> {

                    final Player clickPlayer = click.getPlayer();
                    final Optional<User> clickUserOptional = userService.get(clickPlayer.getName());
                    final Optional<com.minecraftsolutions.economy.model.user.User> clickEconomyUserOptional = api.getUser(clickPlayer.getName());

                    if (!clickUserOptional.isPresent() || !clickEconomyUserOptional.isPresent())
                        return;

                    final com.minecraftsolutions.economy.model.user.User clickEconomyUser = clickEconomyUserOptional.get();
                    final User clickUser = clickUserOptional.get();
                    final FishingRod clickFishingRod = clickUser.getFishingRod();
                    final double clickPrice = enchant.getPrice(clickFishingRod.getEnchantLevel(enchant));

                    if (clickFishingRod.getEnchantLevel(enchant) >= enchant.getMaxLevel()) {
                        clickPlayer.sendMessage(messages.getString("enchant-max-level").replace("&", "§"));
                        return;
                    }
                    if (api.get(enchant.getCurrency(), clickEconomyUser) < clickPrice) {
                        clickPlayer.sendMessage(messages.getString("enchant-no-founds").replace("&", "§"));
                        return;
                    }

                    clickPlayer.sendMessage(messages.getString("enchant-level-up").replace("&", "§"));
                    api.remove(enchant.getCurrency(), clickEconomyUser, clickPrice);
                    clickFishingRod.addEnchant(enchant, clickFishingRod.getEnchantLevel(enchant) + 1);
                    fishingRodService.update(clickFishingRod);
                    userController.setItens(clickUser);
                    click.update();
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
