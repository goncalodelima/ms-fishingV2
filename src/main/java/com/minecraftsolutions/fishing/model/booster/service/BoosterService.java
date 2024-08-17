package com.minecraftsolutions.fishing.model.booster.service;

import com.minecraftsolutions.fishing.model.booster.Booster;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BoosterService implements BoosterFoundationService {

    private final Map<String, Booster> cache = new HashMap<>();

    @Override
    public void put(Booster booster) {
        this.cache.put(booster.getId(), booster);
    }

    @Override
    public Optional<Booster> get(String id) {
        return Optional.ofNullable(this.cache.get(id));
    }
}
