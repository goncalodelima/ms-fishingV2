package com.minecraftsolutions.fishing.model.reward.service;

import com.minecraftsolutions.fishing.model.reward.Reward;

import java.util.ArrayList;
import java.util.List;

public class RewardService implements RewardFoundationService{

    private final List<Reward> cache = new ArrayList<>();

    @Override
    public void put(Reward reward) {
        this.cache.add(reward);
    }

    @Override
    public List<Reward> getAll() {
        return this.cache;
    }
}
