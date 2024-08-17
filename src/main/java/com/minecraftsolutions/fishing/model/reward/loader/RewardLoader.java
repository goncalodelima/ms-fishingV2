package com.minecraftsolutions.fishing.model.reward.loader;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.model.reward.Reward;
import com.minecraftsolutions.fishing.model.reward.adapter.RewardAdapter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public class RewardLoader {

    private final FileConfiguration reward = FishingPlugin.getInstance().getReward().getConfig();
    private final RewardAdapter adapter = new RewardAdapter();

    public List<Reward> setup(){
        return reward.getConfigurationSection("").getKeys(false)
                .stream()
                .map(key -> this.adapter.adapt(reward.getConfigurationSection(key)))
                .collect(Collectors.toList());
    }

}
