package com.minecraftsolutions.fishing.model.enchant.loader;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.model.enchant.Enchant;
import com.minecraftsolutions.fishing.model.enchant.adapter.EnchantAdapter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public class EnchantLoader {

    private final FileConfiguration config = FishingPlugin.getInstance().getEnchant().getConfig();
    private final EnchantAdapter adapter = new EnchantAdapter();

    public List<Enchant> setup(){
        return config.getConfigurationSection("").getKeys(false)
                .stream()
                .map(key -> this.adapter.adapt(config.getConfigurationSection(key)))
                .collect(Collectors.toList());
    }

}
