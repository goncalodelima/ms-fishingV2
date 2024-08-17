package com.minecraftsolutions.fishing.model.fish.service;

import com.minecraftsolutions.fishing.model.fish.Fish;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FishService implements FishFoundationService{

    private final Map<String, Fish> cache = new HashMap<>();

    @Override
    public void put(Fish fish) {
        this.cache.put(fish.getId(), fish);
    }

    @Override
    public Optional<Fish> get(String id) {
        return Optional.ofNullable(this.cache.get(id));
    }

    @Override
    public List<Fish> getAll() {
        return this.cache
                .keySet()
                .stream()
                .map(this::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
