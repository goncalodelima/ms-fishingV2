package com.minecraftsolutions.fishing.model.fishingrod.service;

import com.minecraftsolutions.fishing.model.fishingrod.FishingRod;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface FishingRodFoundationService {

    void put(FishingRod fishingRod);

    void update(FishingRod fishingRod);

    CompletableFuture<Void> update(Collection<FishingRod> fishingRods);

    void remove(FishingRod fishingRod);

    Optional<FishingRod> get(UUID uniqueId);

    Map<UUID, FishingRod> getPendingUpdates();

}
