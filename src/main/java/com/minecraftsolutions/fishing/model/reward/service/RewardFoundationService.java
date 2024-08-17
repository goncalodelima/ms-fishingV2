package com.minecraftsolutions.fishing.model.reward.service;

import com.minecraftsolutions.fishing.model.reward.Reward;

import java.util.List;

public interface RewardFoundationService {

    void put(Reward reward);

    List<Reward> getAll();

}
