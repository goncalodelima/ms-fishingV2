package com.minecraftsolutions.fishing.model.fish.service;

import com.minecraftsolutions.fishing.model.fish.Fish;

import java.util.List;
import java.util.Optional;

public interface FishFoundationService {

    void put(Fish fish);

    Optional<Fish> get(String id);

    List<Fish> getAll();

}
