package com.minecraftsolutions.fishing.model.fishingrod.repository;

import com.minecraftsolutions.fishing.model.fishingrod.FishingRod;

import java.util.Collection;
import java.util.UUID;

public interface FishingRodFoundationRepository {

    void setup();

    void insert(FishingRod fishingRod);

    void update(Collection<FishingRod> fishingRods);

    FishingRod findOne(UUID uniqueId);

}
