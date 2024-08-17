package com.minecraftsolutions.fishing.model.bucket.adapter;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.model.bucket.Bucket;
import com.minecraftsolutions.fishing.model.fish.Fish;
import com.minecraftsolutions.fishing.model.fish.service.FishFoundationService;
import com.minecraftsolutions.database.adapter.DatabaseAdapter;
import com.minecraftsolutions.database.executor.DatabaseQuery;

import java.util.UUID;

public class BucketAdapter implements DatabaseAdapter<Bucket> {

    private final FishFoundationService fishService = FishingPlugin.getInstance().getFishService();

    @Override
    public Bucket adapt(DatabaseQuery databaseQuery) {

        final UUID uniqueId = UUID.fromString((String) databaseQuery.get("uniqueId"));
        final int level = (Integer) databaseQuery.get("level");
        final Bucket bucket = new Bucket(uniqueId, level);

        do {

            if (databaseQuery.get("fish") == null || databaseQuery.get("amount") == null)
                continue;

            final Fish fish = fishService.get((String) databaseQuery.get("fish")).orElseThrow(RuntimeException::new);
            final int amount = (Integer) databaseQuery.get("amount");
            bucket.addFish(fish, amount);

        }while (databaseQuery.next());

        return bucket;
    }
}
