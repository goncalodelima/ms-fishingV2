package com.minecraftsolutions.fishing.model.enchant.service;

import com.minecraftsolutions.fishing.model.enchant.Enchant;
import com.minecraftsolutions.fishing.model.enchant.type.EnchantType;

import java.util.List;
import java.util.Optional;

public interface EnchantFoundationService {

    void put(Enchant enchant);

    Optional<Enchant> get(EnchantType type);

    List<Enchant> getAll();

}
