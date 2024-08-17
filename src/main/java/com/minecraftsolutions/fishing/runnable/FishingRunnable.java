package com.minecraftsolutions.fishing.runnable;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.controller.user.UserController;
import com.minecraftsolutions.fishing.model.bucket.Bucket;
import com.minecraftsolutions.fishing.model.bucket.service.BucketFoundationService;
import com.minecraftsolutions.fishing.model.enchant.service.EnchantFoundationService;
import com.minecraftsolutions.fishing.model.enchant.type.EnchantType;
import com.minecraftsolutions.fishing.model.fish.Fish;
import com.minecraftsolutions.fishing.model.fish.service.FishFoundationService;
import com.minecraftsolutions.fishing.model.fishingrod.FishingRod;
import com.minecraftsolutions.fishing.model.fishingrod.slot.Slot;
import com.minecraftsolutions.fishing.model.reward.Reward;
import com.minecraftsolutions.fishing.model.reward.service.RewardFoundationService;
import com.minecraftsolutions.fishing.model.user.User;
import com.minecraftsolutions.fishing.model.user.fishing.FishingType;
import com.minecraftsolutions.fishing.model.user.service.UserFoundationService;
import com.minecraftsolutions.fishing.util.BukkitUtils;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class FishingRunnable extends BukkitRunnable {

    private final UserFoundationService userService = FishingPlugin.getInstance().getUserService();
    private final EnchantFoundationService enchantService = FishingPlugin.getInstance().getEnchantService();
    private final FishFoundationService fishService = FishingPlugin.getInstance().getFishService();
    private final BucketFoundationService bucketService = FishingPlugin.getInstance().getBucketService();
    private final RewardFoundationService rewardService = FishingPlugin.getInstance().getRewardService();
    private final UserController userController = FishingPlugin.getInstance().getUserController();
    private final char[] status = new char[]{'|', '/', '-', '\\'};
    private int timeAnim = 40;
    private final Random random = new Random();

    @Override
    public void run() {

        if (timeAnim < 0)
            timeAnim = 40;

        int i = timeAnim / 10;
        i = i == 4 ? 3 : i;

        for (User user : userService.getAll().stream().filter(user -> user.getFishingType().equals(FishingType.FISHING)).collect(Collectors.toList())) {

            if (timeAnim % 10 == 0)
                BukkitUtils.sendActionBar(user.getPlayer(), "&ePescando &b" + status[i]);

            FishingRod fishingRod = user.getFishingRod();
            Bucket bucket = user.getBucket();

            List<EnchantType> enchants = new ArrayList<>();

            for (Slot slot : fishingRod.getSlots()) {

                if (!slot.hasEnchant()) {
                    continue;
                }

                enchants.add(slot.getEnchant().getType());
            }

            AtomicInteger fishes = new AtomicInteger();
            enchantService.get(EnchantType.FISHER).ifPresent(enchant -> {
                double range = fishingRod.getEnchants().getOrDefault(enchant, 0.0);
                fishes.set(range <= 0 ? 0 : random.nextInt((int) range));
            });

            AtomicReference<Double> luckBonus = new AtomicReference<>((double) 0);
            enchantService.get(EnchantType.LUCK).ifPresent(enchant -> {
                luckBonus.set(fishingRod.getEnchants().getOrDefault(enchant, 0.0));
            });

            AtomicReference<Double> luckyBonus = new AtomicReference<>((double) 0);
            enchantService.get(EnchantType.LUCKY).ifPresent(enchant -> {
                luckyBonus.set(fishingRod.getEnchants().getOrDefault(enchant, 0.0));
            });

            user.setTime(user.getTime() - 1);

            if (user.getTime() % 5 == 0 && enchants.contains(EnchantType.LUCKY)) {

                for (Reward reward : rewardService.getAll()) {

                    if (reward.getChance() + luckyBonus.get() > random.nextDouble())
                        reward.give(user.getPlayer());

                }

            }

            user.addFishingTime(1);
            user.getBoosters().keySet().forEach(booster -> {
                user.getBoosters().put(booster, user.getBoosters().get(booster) - 1);
                if (user.getBoosters().getOrDefault(booster, 0) < 0)
                    user.getBoosters().remove(booster);
            });

            if (user.getTime() <= 0) {

                Map<Object, Double> chance = new HashMap<>();
                fishService.getAll().forEach(fish -> chance.put(fish, fish.getChance() + (enchants.contains(EnchantType.LUCKY) ? luckBonus.get() : 0)));
                Fish fish = (Fish) BukkitUtils.randomObject(chance);

                if (fish != null) {
                    int amount = 3 + (enchants.contains(EnchantType.FISHER) ? fishes.get() : 0);
                    amount += (int) (amount * user.getBoosterMultiplier());
                    bucket.addFish(fish, amount);
                    BukkitUtils.sendTitle(user.getPlayer(), "§a&lPESCA", "&fx" + FishingPlugin.FORMATTER.formatNumber(amount) + " " + fish.getName(), 5, 20, 5);
                    bucketService.update(bucket);
                    user.getPlayer().getInventory().setItem(Bucket.SLOT, bucket.getItem());
                    user.addHookedFish(amount);
                    userService.update(user);
                    userService.updateBoosters(user);
                }

                enchantService.get(EnchantType.FAST).ifPresent(enchant -> {
                    user.setTime(FishingRod.DEFAULT_TIME - (enchants.contains(EnchantType.FAST) ? 0 : fishingRod.getEnchants().getOrDefault(enchant, 0.0) * 20));
                });

            }

        }

        timeAnim--;
    }

}
