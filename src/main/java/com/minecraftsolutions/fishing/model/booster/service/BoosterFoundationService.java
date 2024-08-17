package com.minecraftsolutions.fishing.model.booster.service;

import com.minecraftsolutions.fishing.model.booster.Booster;

import java.util.Optional;

public interface BoosterFoundationService {

    void put(Booster booster);

    Optional<Booster> get(String id);

}
