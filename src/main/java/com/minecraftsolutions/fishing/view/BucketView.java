package com.minecraftsolutions.fishing.view;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.model.bucket.Bucket;
import com.minecraftsolutions.fishing.model.bucket.service.BucketFoundationService;
import com.minecraftsolutions.fishing.model.fish.Fish;
import com.minecraftsolutions.fishing.model.user.User;
import com.minecraftsolutions.fishing.model.user.service.UserFoundationService;
import com.minecraftsolutions.fishing.util.item.ItemBuilder;
import lombok.AllArgsConstructor;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class BucketView extends View {

    private final FileConfiguration messages = FishingPlugin.getInstance().getMessages().getConfig();
    private final FileConfiguration config = FishingPlugin.getInstance().getBucketMenu().getConfig();
    private final UserFoundationService userService = FishingPlugin.getInstance().getUserService();
    private final BucketFoundationService bucketService = FishingPlugin.getInstance().getBucketService();
    private final List<Fish> fishes;

    private final State<Pagination> paginationState;

    private final State<ItemStack> bucketUpgradeState = computedState(context -> {

       final Player player = context.getPlayer();
       final Optional<User> userOptional = userService.get(player.getName());
       if (!userOptional.isPresent())
           return new ItemStack(Material.AIR);

       final User user = userOptional.get();
       final Bucket bucket = user.getBucket();
       final int currentLevel = bucket.getLevel();
       final int nextLevel = currentLevel + 1;
       final double price = bucket.getPrice();
       String status;

       if (bucket.getLevel() >= Bucket.MAX_LEVEL)
           status = config.getString("upgrade.status.max-level");
       else if (Bucket.CURRENCY.get(player) < price)
           status = config.getString("upgrade.status.no-founds");
       else status = config.getString("upgrade.status.has-founds");

       return new ItemBuilder(Material.getMaterial(config.getString("upgrade.material")), 1, (short) config.getInt("upgrade.data"))
               .setSkull(config.getString("upgrade.url"))
               .setDisplayName(config.getString("upgrade.name").replace("{current_level}", FishingPlugin.FORMATTER.formatNumber(currentLevel))
                       .replace("{next_level}", FishingPlugin.FORMATTER.formatNumber(nextLevel)))
               .setLore(config.getStringList("upgrade.lore").stream().map(str -> str.replace("{icon}", Bucket.CURRENCY.getIcon())
                       .replace("{price}", FishingPlugin.FORMATTER.formatNumber(price))
                       .replace("{current_capacity}", FishingPlugin.FORMATTER.formatNumber(bucket.getCapacity()))
                       .replace("{max_capacity}", FishingPlugin.FORMATTER.formatNumber(bucket.getMaxCapacity())))
                       .collect(Collectors.toList()))
               .updateLore("{status}", status.replace("&", "§"))
               .glow(config.getBoolean("upgrade.glow"))
               .build();
    });

    private final State<ItemStack> earnsState = computedState(context -> {

        final Player player = context.getPlayer();
        final Optional<User> userOptional = userService.get(player.getName());

        if (!userOptional.isPresent())
            return new ItemStack(Material.AIR);

        final User user = userOptional.get();
        final Bucket bucket = user.getBucket();

        if (bucket.hasEarnsToReceive()){

            return new ItemBuilder(Material.getMaterial(config.getString("sell.no-empty.material")), 1, (short) config.getInt("sell.no-empty.data"))
                    .setSkull(config.getString("sell.no-empty.url"))
                    .setDisplayName(config.getString("sell.no-empty.name"))
                    .setLore(config.getStringList("sell.no-empty.lore")
                            .stream().map(str -> str.replace("{amount}", FishingPlugin.FORMATTER.formatNumber(bucket.getFishesCount())))
                            .collect(Collectors.toList()))
                    .updateLore("{profit}", profitFormatted(bucket))
                    .glow(config.getBoolean("sell.no-empty.glow"))
                    .build();

        }

        return new ItemBuilder(Material.getMaterial(config.getString("sell.empty.material")), 1, (short) config.getInt("sell.empty.data"))
                .setSkull(config.getString("sell.empty.url"))
                .setDisplayName(config.getString("sell.empty.name"))
                .setLore(config.getStringList("sell.empty.lore"))
                .glow(config.getBoolean("sell.empty.glow"))
                .build();

    });

    public BucketView(List<Fish> fishes){

        this.fishes = fishes;

        paginationState = buildLazyPaginationState(c -> this.fishes)
                .layoutTarget('F')
                .itemFactory((bukkitItemComponentBuilder, fish) -> {

                    bukkitItemComponentBuilder.onRender(render -> {

                    final Player player = render.getPlayer();
                    final Optional<User> userOptional = userService.get(player.getName());

                    if (!userOptional.isPresent())
                        return;

                    final User user = userOptional.get();
                    final Bucket bucket = user.getBucket();
                    final int amount = bucket.getFishAmount(fish);
                    ItemStack item = fish.getDisplay().clone();
                    item.setAmount(Math.min(amount, 64));
                    ItemBuilder builder = new ItemBuilder(item)
                            .changeItemMeta(meta -> meta.setLore(meta.getLore().stream().map(str -> str.replace("{amount}", FishingPlugin.FORMATTER.formatNumber(amount))).collect(Collectors.toList())));

                    fish.getCurrencies().forEach( (currency, priceBase) -> {
                        double finalPrice = priceBase * amount;
                        builder.changeItemMeta(meta -> meta.setLore(meta.getLore().stream().map(str -> str.replace("{" + currency.getName().toUpperCase() + "}", FishingPlugin.FORMATTER.formatNumber(finalPrice))).collect(Collectors.toList())));
                    });

                        render.setItem(builder.build());
                    }).onClick(click -> {

                        final Player player = click.getPlayer();
                        final Optional<User> userOptional = userService.get(player.getName());

                        if (!userOptional.isPresent())
                            return;

                        final User user = userOptional.get();
                        final Bucket bucket = user.getBucket();
                        final int amount = bucket.getFishAmount(fish);

                        if (amount <= 0)
                            return;

                        fish.getCurrencies().forEach((currency, priceBase) -> {
                            final double finalPrice = priceBase * amount;
                            currency.give(player, finalPrice);
                        });
                        bucket.setFish(fish, 0);
                        bucketService.update(bucket);
                        click.update();
                    });

                })
                .build();

    }

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

        render.layoutSlot('S')
                        .watch(earnsState)
                        .renderWith(() -> earnsState.get(render))
                        .onClick(click -> {

                            final Player player = click.getPlayer();
                            final Optional<User> userOptional = userService.get(player.getName());

                            if (!userOptional.isPresent())
                                return;

                            final User user = userOptional.get();
                            final Bucket bucket = user.getBucket();

                            if (!bucket.hasEarnsToReceive())
                                return;

                            List<String> profit = new ArrayList<>();

                            bucket.getFishesPrice().forEach( (currency, price) -> {
                                currency.give(player, price);
                                profit.add(messages.getString("sell-format").replace("&", "§")
                                        .replace("{icon}", currency.getIcon())
                                        .replace("{price}", FishingPlugin.FORMATTER.formatNumber(price)));
                            });

                            for (String msg : messages.getStringList("sell-all")){

                                if (msg.equals("{prices}"))
                                    profit.forEach(player::sendMessage);
                                else player.sendMessage(msg.replace("&", "§"));

                            }

                            bucket.resetFishes();
                            bucketService.update(bucket);
                            click.update();
                        });

        render.layoutSlot('B', back)
                .onClick(click -> {

                    Player clickPlayer = click.getPlayer();
                    click.closeForPlayer();
                    FishingPlugin.getInstance().getViewFrame().open(FishingExitView.class, clickPlayer);

                });

        render.layoutSlot('U')
                .watch(bucketUpgradeState)
                .renderWith(() -> bucketUpgradeState.get(render))
                .onClick(click -> {

                    final Player player = click.getPlayer();
                    final Optional<User> userOptional = userService.get(player.getName());

                    if (!userOptional.isPresent())
                        return;

                    final User user = userOptional.get();
                    final Bucket bucket = user.getBucket();
                    final int currentLevel = bucket.getLevel();
                    final int nextLevel = currentLevel + 1;
                    final double price = bucket.getPrice();

                    if (Bucket.CURRENCY.get(player) < price)
                        return;

                    Bucket.CURRENCY.remove(player, price);
                    player.sendMessage(messages.getString("upgrade-bucket").replace("&", "§"));
                    bucket.setLevel(nextLevel);
                    bucketService.update(bucket);
                    click.update();
                });

    }

    private List<String> profitFormatted(Bucket bucket){

        final List<String> list = new ArrayList<>();

        bucket.getFishesPrice().forEach( ((currency, earns) -> {
            list.add(config.getString("sell.no-empty.sell-format").replace("{name}", currency.getName())
                    .replace("{icon}", currency.getIcon())
                    .replace("{price}", FishingPlugin.FORMATTER.formatNumber(earns)));
        }));

        return list;
    }

}
