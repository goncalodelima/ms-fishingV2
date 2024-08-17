package com.minecraftsolutions.fishing.model.enchant.service;

import com.minecraftsolutions.fishing.model.enchant.Enchant;
import com.minecraftsolutions.fishing.model.enchant.type.EnchantType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EnchantService implements EnchantFoundationService {

    private final Map<EnchantType, Enchant> cache = new HashMap<>();

    @Override
    public void put(Enchant enchant) {
        this.cache.put(enchant.getType(), enchant);
    }

    @Override
    public Optional<Enchant> get(EnchantType type) {
        return Optional.ofNullable(this.cache.get(type));
    }

    @Override
    public List<Enchant> getAll() {
        return this.cache
                .keySet()
                .stream()
                .map(this::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
