package com.minecraftsolutions.fishing.model.booster.loader;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.model.booster.Booster;
import com.minecraftsolutions.fishing.model.booster.adapter.BoosterAdapter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public class BoosterLoader {

    private final FileConfiguration config = FishingPlugin.getInstance().getBooster().getConfig();
    private final BoosterAdapter adapter = new BoosterAdapter();

    public List<Booster> setup(){
        return config.getConfigurationSection("").getKeys(false)
                .stream()
                .map(key -> this.adapter.adapt(config.getConfigurationSection(key)))
                .collect(Collectors.toList());
    }

}
