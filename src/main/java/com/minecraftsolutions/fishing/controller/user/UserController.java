package com.minecraftsolutions.fishing.controller.user;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.controller.user.exception.EmptyInventoryException;
import com.minecraftsolutions.fishing.controller.user.exception.LocationException;
import com.minecraftsolutions.fishing.model.bucket.Bucket;
import com.minecraftsolutions.fishing.model.enchant.service.EnchantFoundationService;
import com.minecraftsolutions.fishing.model.enchant.type.EnchantType;
import com.minecraftsolutions.fishing.model.fishingrod.FishingRod;
import com.minecraftsolutions.fishing.model.user.User;
import com.minecraftsolutions.fishing.model.user.fishing.FishingType;
import com.minecraftsolutions.fishing.model.user.service.UserFoundationService;
import com.minecraftsolutions.fishing.runnable.FishingRunnable;
import com.minecraftsolutions.fishing.util.BukkitUtils;
import com.minecraftsolutions.fishing.util.LocationSerializer;
import com.minecraftsolutions.fishing.util.item.ItemBuilder;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
public class UserController {

    private int fishers;
    private List<User> topByTime = new ArrayList<>();
    private List<User> topByFishes = new ArrayList<>();
    private final UserFoundationService userService = FishingPlugin.getInstance().getUserService();
    private final EnchantFoundationService enchantService = FishingPlugin.getInstance().getEnchantService();
    private final FileConfiguration config = FishingPlugin.getInstance().getConfig();
    private final FileConfiguration locations = FishingPlugin.getInstance().getLocations().getConfig();
    private final ItemStack exit = new ItemBuilder(Material.getMaterial(config.getString("exit.material")), 1, (short) config.getInt("exit.data"))
            .setDisplayName(config.getString("exit.name"))
            .setLore(config.getStringList("exit.lore"))
            .glow(config.getBoolean("exit.glow"))
            .build();

    public UserController(){

        new FishingRunnable().runTaskTimer(FishingPlugin.getInstance(), 0, 1);

        new BukkitRunnable() {
            @Override
            public void run() {

                List<User> topByTime = userService.getTopByTime();
                topByTime.sort(Comparator.comparingInt(User::getFishingTime).reversed());
                List<User> topByFishes = userService.getTopByFishes();
                topByFishes.sort(Comparator.comparingInt(User::getHookedFish).reversed());

                UserController.this.topByTime = topByTime;
                UserController.this.topByFishes = topByFishes;

            }
        }.runTaskTimerAsynchronously(FishingPlugin.getInstance(), 20, 20 * 60 * 10);

    }

    public void startFishing(User user){

        Player player = user.getPlayer();
        FishingRod fishingRod = user.getFishingRod();
        enchantService.getAll().forEach(enchant -> enchant.run(user));
        enchantService.get(EnchantType.FAST).ifPresent(enchant -> user.setTime(FishingRod.DEFAULT_TIME - fishingRod.getEnchants().getOrDefault(enchant, 0.0) * 20));
        user.setFishingType(FishingType.FISHING);
        BukkitUtils.sendTitle(player, "&a&lPESCA", "&eVocê começou a pescar", 5, 20, 5);
    }

    public void stopFishing(User user){

        Player player = user.getPlayer();
        user.setTime(FishingRod.DEFAULT_TIME);
        user.setFishingType(FishingType.IN);
        BukkitUtils.sendTitle(player, "&a&lPESCA", "&cVocê parou de pescar", 5, 20, 5);
    }

    public void enterFishingArea(User user) throws LocationException, EmptyInventoryException {

        if (user.getFishingType().equals(FishingType.IN) || user.getFishingType().equals(FishingType.FISHING))
            return;

        if (!locations.contains("spawn") || !locations.contains("exit"))
            throw new LocationException();

        final Player player = user.getPlayer();

        if (isOccuped(player.getInventory(), FishingRod.SLOT) || isOccuped(player.getInventory(), Bucket.SLOT) || isOccuped(player.getInventory(), config.getInt("exit.slot")))
            throw new EmptyInventoryException();

        final Location spawn = LocationSerializer.deserializeLocation(locations.getString("spawn"));
        user.setFishingType(FishingType.IN);
        player.teleport(spawn);
        setItens(user);
        fishers++;
    }

    public void exitFishingArea(User user){

        if (user.getFishingType().equals(FishingType.OUT))
            return;

        final Player player = user.getPlayer();
        final Location exit = LocationSerializer.deserializeLocation(locations.getString("exit"));
        player.getInventory().clear(config.getInt("exit.slot"));
        player.getInventory().clear(Bucket.SLOT);
        player.getInventory().clear(FishingRod.SLOT);
        user.setFishingType(FishingType.OUT);
        player.teleport(exit);
        fishers--;
    }

    public void setItens(User user){

        final Player player = user.getPlayer();
        player.getInventory().setItem(config.getInt("exit.slot"), exit);
        player.getInventory().setItem(Bucket.SLOT, user.getBucket().getItem());
        player.getInventory().setItem(FishingRod.SLOT, user.getFishingRod().getItem());

    }

    private boolean isOccuped(Inventory inventory, int slot){
        ItemStack item = inventory.getItem(slot);
        return item != null && item.getType() != Material.AIR;
    }

}
