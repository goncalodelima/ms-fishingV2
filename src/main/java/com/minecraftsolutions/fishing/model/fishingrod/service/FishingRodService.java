package com.minecraftsolutions.fishing.model.fishingrod.service;

import com.minecraftsolutions.fishing.model.fishingrod.FishingRod;
import com.minecraftsolutions.fishing.model.fishingrod.repository.FishingRodFoundationRepository;
import com.minecraftsolutions.fishing.model.fishingrod.repository.FishingRodRepository;
import com.minecraftsolutions.database.Database;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FishingRodService implements FishingRodFoundationService{

    private final Map<UUID, FishingRod> cache = new ConcurrentHashMap<>();
    private final Map<UUID, FishingRod> pendingUpdates = new HashMap<>();
    private final FishingRodFoundationRepository fishingRodRepository;

    public FishingRodService(Database database){
        this.fishingRodRepository = new FishingRodRepository(database);
        this.fishingRodRepository.setup();
    }

    @Override
    public void put(FishingRod fishingRod) {
        this.cache.put(fishingRod.getUniqueId(), fishingRod);
        this.fishingRodRepository.insert(fishingRod);
    }

    @Override
    public void update(FishingRod fishingRod) {
        this.pendingUpdates.put(fishingRod.getUniqueId(), fishingRod);
    }

    @Override
    public void update(Collection<FishingRod> fishingRods) {
        this.fishingRodRepository.update(fishingRods);
    }

    @Override
    public void remove(FishingRod fishingRod) {
        this.cache.remove(fishingRod.getUniqueId());
    }

    @Override
    public Optional<FishingRod> get(UUID uniqueId) {

        FishingRod fishingRod = this.cache.get(uniqueId);

        if (fishingRod != null)
            return Optional.of(fishingRod);

        fishingRod = this.fishingRodRepository.findOne(uniqueId);

        if (fishingRod != null)
            this.cache.put(fishingRod.getUniqueId(), fishingRod);

        return Optional.ofNullable(fishingRod);
    }

    @Override
    public Map<UUID, FishingRod> getPendingUpdates() {
        return pendingUpdates;
    }

}
