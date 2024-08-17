package com.minecraftsolutions.fishing.view;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.controller.user.UserController;
import com.minecraftsolutions.fishing.util.BukkitUtils;
import com.minecraftsolutions.fishing.util.item.ItemBuilder;
import me.devnatan.inventoryframework.View;
import me.devnatan.inventoryframework.ViewConfigBuilder;
import me.devnatan.inventoryframework.component.Pagination;
import me.devnatan.inventoryframework.context.RenderContext;
import me.devnatan.inventoryframework.state.MutableIntState;
import me.devnatan.inventoryframework.state.State;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Collectors;

public class RankingView extends View {

    private final FileConfiguration config = FishingPlugin.getInstance().getRanking().getConfig();
    private final UserController userController = FishingPlugin.getInstance().getUserController();
    private final MutableIntState filterStatePosition = mutableState(0);

    private final State<ItemStack> filterState = computedState(context -> {

        int pos = filterStatePosition.get(context);

        return new ItemBuilder(Material.getMaterial(config.getString("ranking.filter.material")), 1, (short) config.getInt("ranking.filter.data"))
                .setSkull(config.contains("ranking.filter.url") ? config.getString("ranking.filter.url") : null)
                .setDisplayName(config.getString("ranking.filter.name"))
                .setLore(config.getStringList("ranking.filter.option-" + pos + ".lore"))
                .glow(config.getBoolean("ranking.filter.glow"))
                .build();

    });

    private final State<Pagination> paginationState = computedPaginationState(
            context -> filterStatePosition.get(context) == 0 ? userController.getTopByFishes() : userController.getTopByTime(),
            (c, itemBuilder, index, user) -> itemBuilder.onRender(context -> {

                if (filterStatePosition.get(context) == 0) {

                    int pos = userController.getTopByFishes().indexOf(user) + 1;

                    context.setItem(new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3)
                            .changeSkullMeta(meta -> meta.setOwner(user.getNickname()))
                            .setDisplayName(config.getString("ranking.fish.name").replace("{player}", user.getNickname()).replace("{pos}", String.valueOf(pos)))
                            .setLore(config.getStringList("ranking.fish.lore").stream().map(str -> str.replace("{amount}", FishingPlugin.FORMATTER.formatNumber(user.getHookedFish()))).collect(Collectors.toList()))
                            .build());

                }else{

                    int pos = userController.getTopByTime().indexOf(user) + 1;

                    context.setItem(new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3)
                            .changeSkullMeta(meta -> meta.setOwner(user.getNickname()))
                            .setDisplayName(config.getString("ranking.time.name").replace("{player}", user.getNickname()).replace("{pos}", String.valueOf(pos)))
                            .setLore(config.getStringList("ranking.time.lore").stream().map(str -> str.replace("{time}", BukkitUtils.formatSeconds((int) (user.getFishingTime() / 20)))).collect(Collectors.toList()))
                            .build());

                }

            }));

    @Override
    public void onInit(ViewConfigBuilder config) {

        config
                .title(this.config.getString("ranking.title").replace("&", "§"))
                .size(this.config.getInt("ranking.size"))
                .layout(this.config.getStringList("ranking.layout").toArray(new String[0]))
                .cancelOnClick()
                .cancelOnDrag()
                .cancelOnDrop()
                .cancelOnPickup()
                .build();

    }

    @Override
    public void onFirstRender(RenderContext render) {

        paginationState.get(render).getComponents().forEach(render::addComponent);

        render.layoutSlot('B', new ItemBuilder(Material.getMaterial(config.getString("ranking.back.material")), 1, (short) config.getInt("ranking.back.data"))
                .setSkull(config.contains("ranking.back.url") ? config.getString("ranking.back.url") : null)
                .setDisplayName(config.getString("ranking.back.name"))
                .build()).onClick(click -> {

            Player player = click.getPlayer();
            click.closeForPlayer();
            FishingPlugin.getInstance().getViewFrame().open(FishingExitView.class, player);

        });

        render.layoutSlot('F')
                .watch(filterStatePosition)
                .onRender(r -> r.setItem(filterState.get(render)))
                .onClick(click -> {

                    if (filterStatePosition.get(render) == 0) filterStatePosition.increment(render);
                    else filterStatePosition.decrement(render);

                    click.update();
                    paginationState.get(click).forceUpdate();
                });


    }

}
