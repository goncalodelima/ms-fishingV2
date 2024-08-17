package com.minecraftsolutions.fishing.model.user.adapter;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.model.booster.Booster;
import com.minecraftsolutions.fishing.model.booster.service.BoosterFoundationService;
import com.minecraftsolutions.fishing.model.bucket.Bucket;
import com.minecraftsolutions.fishing.model.bucket.service.BucketFoundationService;
import com.minecraftsolutions.fishing.model.fishingrod.FishingRod;
import com.minecraftsolutions.fishing.model.fishingrod.service.FishingRodFoundationService;
import com.minecraftsolutions.fishing.model.user.User;
import com.minecraftsolutions.fishing.model.user.fishing.FishingType;
import lombok.SneakyThrows;
import com.minecraftsolutions.database.adapter.DatabaseAdapter;
import com.minecraftsolutions.database.executor.DatabaseQuery;

import java.util.UUID;

public class UserAdapter implements DatabaseAdapter<User> {

    private final FishingRodFoundationService fishingRodService = FishingPlugin.getInstance().getFishingRodService();
    private final BucketFoundationService bucketService = FishingPlugin.getInstance().getBucketService();
    private final BoosterFoundationService boosterService = FishingPlugin.getInstance().getBoosterService();

    @SneakyThrows
    @Override
    public User adapt(DatabaseQuery databaseQuery) {

        final String nickname = (String) databaseQuery.get("nickname");
        final FishingRod fishingRod = fishingRodService.get(UUID.fromString((String) databaseQuery.get("fishingrod"))).orElseThrow(RuntimeException::new);
        final Bucket bucket = bucketService.get(UUID.fromString((String) databaseQuery.get("bucket"))).orElseThrow(RuntimeException::new);
        final int hookedFish = (Integer) databaseQuery.get("hookedFish");
        final int fishingTime = (Integer) databaseQuery.get("fishingTime");

        User user = new User(nickname, fishingRod, bucket, hookedFish, fishingTime, FishingType.OUT, FishingRod.DEFAULT_TIME);

        do {

            if (databaseQuery.get("booster") == null || databaseQuery.get("time") == null)
                continue;

            Booster booster = boosterService.get((String) databaseQuery.get("booster")).orElseThrow(RuntimeException::new);
            int time = (Integer) databaseQuery.get("time") / 20;
            user.addBooster(booster, time);
        }while (databaseQuery.next());

        return user;
    }
}
