package com.minecraftsolutions.fishing.listener;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.controller.booster.BoosterController;
import com.minecraftsolutions.fishing.controller.user.UserController;
import com.minecraftsolutions.fishing.model.booster.Booster;
import com.minecraftsolutions.fishing.model.bucket.Bucket;
import com.minecraftsolutions.fishing.model.bucket.service.BucketFoundationService;
import com.minecraftsolutions.fishing.model.fishingrod.FishingRod;
import com.minecraftsolutions.fishing.model.fishingrod.service.FishingRodFoundationService;
import com.minecraftsolutions.fishing.model.user.User;
import com.minecraftsolutions.fishing.model.user.fishing.FishingType;
import com.minecraftsolutions.fishing.model.user.service.UserFoundationService;
import com.minecraftsolutions.fishing.util.item.ItemNBT;
import com.minecraftsolutions.fishing.view.BucketView;
import com.minecraftsolutions.fishing.view.FishingRodManagerView;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;

import java.util.Optional;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final FileConfiguration config = FishingPlugin.getInstance().getMessages().getConfig();
    private final UserFoundationService userService = FishingPlugin.getInstance().getUserService();
    private final FishingRodFoundationService fishingRodService = FishingPlugin.getInstance().getFishingRodService();
    private final BucketFoundationService bucketService = FishingPlugin.getInstance().getBucketService();
    private final BoosterController boosterController = FishingPlugin.getInstance().getBoosterController();
    private final UserController userController = FishingPlugin.getInstance().getUserController();

    @EventHandler
    void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {

        Optional<User> userOptional = userService.get(event.getName());

        if (!userOptional.isPresent()) {

            FishingRod fishingRod = new FishingRod(UUID.randomUUID());
            Bucket bucket = new Bucket(UUID.randomUUID());
            User user = new User(event.getName(), fishingRod, bucket, 0, 0, FishingType.OUT, 0);

            fishingRodService.put(fishingRod);
            bucketService.put(bucket);

            userService.putData(user);
            userService.put(user);

        }

    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event){

        Player player = event.getPlayer();
        userService.get(player.getName()).ifPresent(user -> {
            userController.exitFishingArea(user);
            userService.remove(user);
        });

    }

    @EventHandler
    void onPlayerInteract(PlayerInteractEvent event){

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();

        if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR)
            return;

        Optional<User> userOptional = userService.get(player.getName());

        if (!userOptional.isPresent())
            return;

        User user = userOptional.get();

        if (user.getFishingType().equals(FishingType.IN) && player.getItemInHand() != null && ItemNBT.hasTag(player.getItemInHand(), "fishing-bucket")){
            FishingPlugin.getInstance().getViewFrame().open(BucketView.class, player);
            event.setCancelled(true);
            return;
        }

        if (user.getFishingType().equals(FishingType.IN) && player.isSneaking() && player.getItemInHand().isSimilar(user.getFishingRod().getItem())){
            FishingPlugin.getInstance().getViewFrame().open(FishingRodManagerView.class, player);
            event.setCancelled(true);
            return;
        }

        if (player.getItemInHand().isSimilar(userController.getExit())){
            userController.exitFishingArea(user);
            event.setCancelled(true);
            return;
        }

        Optional<Booster> boosterOptional = boosterController.getByItem(player.getItemInHand());

        if (!boosterOptional.isPresent())
            return;

        event.setCancelled(true);
        Booster booster = boosterOptional.get();
        int time = boosterController.getBoosterTime(player.getItemInHand());

        try {
            user.addBooster(booster, time);
        } catch (Exception e) {
            player.sendMessage(config.getString("max-booster").replace("&", "§"));
            return;
        }

        userService.updateBoosters(user);

        if (player.getItemInHand().getAmount() > 1) player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
        else player.setItemInHand(null);
    }

    @EventHandler
    void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){

        Player player = event.getPlayer();
        Optional<User> userOptional = userService.get(player.getName());

        if (!userOptional.isPresent())
            return;

        User user = userOptional.get();

        if ((user.getFishingType().equals(FishingType.IN) || user.getFishingType().equals(FishingType.FISHING)) && !event.getMessage().equalsIgnoreCase("/pesca"))
            event.setCancelled(true);

    }

    @EventHandler
    void onPlayerDropItem(PlayerDropItemEvent event){

        Player player = event.getPlayer();
        Optional<User> userOptional = userService.get(player.getName());

        if (!userOptional.isPresent())
            return;

        User user = userOptional.get();

        if (user.getFishingType().equals(FishingType.IN) || user.getFishingType().equals(FishingType.FISHING))
            event.setCancelled(true);

    }

    @EventHandler
    void onBlockPlace(BlockPlaceEvent event){

        Player player = event.getPlayer();
        Optional<User> userOptional = userService.get(player.getName());

        if (!userOptional.isPresent())
            return;

        User user = userOptional.get();

        if (user.getFishingType().equals(FishingType.IN) || user.getFishingType().equals(FishingType.FISHING))
            event.setCancelled(true);

    }

    @EventHandler
    void onBlockBreak(BlockBreakEvent event){

        Player player = event.getPlayer();
        Optional<User> userOptional = userService.get(player.getName());

        if (!userOptional.isPresent())
            return;

        User user = userOptional.get();

        if (user.getFishingType().equals(FishingType.IN) || user.getFishingType().equals(FishingType.FISHING))
            event.setCancelled(true);

    }

    @EventHandler
    void onPlayerItemHeld(PlayerItemHeldEvent event){

        Player player = event.getPlayer();
        Optional<User> userOptional = userService.get(player.getName());

        if (!userOptional.isPresent())
            return;

        User user = userOptional.get();
        if (user.getFishingType().equals(FishingType.FISHING))
            userController.stopFishing(user);

    }

    @EventHandler
    void onInventoryClick(InventoryClickEvent event){

        Player player = (Player) event.getWhoClicked();
        Optional<User> userOptional = userService.get(player.getName());

        if (!userOptional.isPresent() || event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.PLAYER)
            return;

        User user = userOptional.get();

        if (user.getFishingType().equals(FishingType.IN) || user.getFishingType().equals(FishingType.FISHING))
            event.setCancelled(true);
    }

    @EventHandler
    void onInventoryOpen(InventoryOpenEvent event){

        Player player = (Player) event.getPlayer();
        Optional<User> userOptional = userService.get(player.getName());

        if (!userOptional.isPresent())
            return;

        User user = userOptional.get();
        if (user.getFishingType().equals(FishingType.FISHING))
            userController.stopFishing(user);
    }

    @EventHandler
    void onPlayerFish(PlayerFishEvent event){

        Player player = event.getPlayer();
        Optional<User> userOptional = userService.get(player.getName());

        if (!userOptional.isPresent())
            return;

        User user = userOptional.get();

        if (!player.getItemInHand().isSimilar(user.getFishingRod().getItem()))
            return;

        if (event.getState() == PlayerFishEvent.State.FISHING) userController.startFishing(user);
        else userController.stopFishing(user);

    }

}
