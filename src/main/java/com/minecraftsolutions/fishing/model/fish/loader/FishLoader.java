package com.minecraftsolutions.fishing.model.fish.loader;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.model.fish.Fish;
import com.minecraftsolutions.fishing.model.fish.adapter.FishAdapter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public class FishLoader {

    private final FileConfiguration config = FishingPlugin.getInstance().getFish().getConfig();
    private final FishAdapter adapter = new FishAdapter();

    public List<Fish> setup(){
        return config.getConfigurationSection("").getKeys(false)
                .stream()
                .map(key -> this.adapter.adapt(config.getConfigurationSection(key)))
                .collect(Collectors.toList());
    }

}
