package com.minecraftsolutions.fishing.model.reward.adapter;

import com.minecraftsolutions.fishing.model.reward.Reward;
import com.minecraftsolutions.fishing.util.configuration.ConfigurationAdapter;
import org.bukkit.configuration.ConfigurationSection;

public class RewardAdapter implements ConfigurationAdapter<Reward> {
    @Override
    public Reward adapt(ConfigurationSection section) {
        return new Reward(section.getString("command"), section.getDouble("chance"));
    }
}
