package com.minecraftsolutions.fishing;

import com.minecraftsolutions.fishing.model.bucket.Bucket;
import com.minecraftsolutions.fishing.model.fishingrod.FishingRod;
import com.minecraftsolutions.fishing.model.user.User;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class UpdateRunnable extends BukkitRunnable {

    @Override
    public void run() {

        Map<UUID, Bucket> buckets = FishingPlugin.getInstance().getBucketService().getPendingUpdates();
        Map<UUID, FishingRod> fishingRods = FishingPlugin.getInstance().getFishingRodService().getPendingUpdates();
        Map<String, User> users = FishingPlugin.getInstance().getUserService().getPendingUpdates();

        FishingPlugin.getInstance().getBucketService().update(new ArrayList<>(buckets.values()));
        FishingPlugin.getInstance().getFishingRodService().update(new ArrayList<>(fishingRods.values()));
        FishingPlugin.getInstance().getUserService().update(new ArrayList<>(users.values()));

        buckets.clear();
        fishingRods.clear();
        users.clear();

    }

}
