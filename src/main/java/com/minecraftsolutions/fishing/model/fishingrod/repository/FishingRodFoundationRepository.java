package com.minecraftsolutions.fishing.model.fishingrod.repository;

import com.minecraftsolutions.fishing.model.fishingrod.FishingRod;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface FishingRodFoundationRepository {

    void setup();

    void insert(FishingRod fishingRod);

    CompletableFuture<Void> update(Collection<FishingRod> fishingRods);

    FishingRod findOne(UUID uniqueId);

}
