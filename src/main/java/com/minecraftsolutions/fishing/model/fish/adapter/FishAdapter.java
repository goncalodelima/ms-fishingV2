package com.minecraftsolutions.fishing.model.fish.adapter;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.hook.EconomyAPI;
import com.minecraftsolutions.fishing.model.fish.Fish;
import com.minecraftsolutions.fishing.util.configuration.ConfigurationAdapter;
import com.minecraftsolutions.fishing.util.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class FishAdapter implements ConfigurationAdapter<Fish> {

    @Override
    public Fish adapt(ConfigurationSection section) {

        final String id = section.getName();
        final String name = section.getString("name").replace("&", "§");
        final double chance = section.getDouble("chance");
        final Map<EconomyAPI, Double> currencies = new HashMap<>();

        for (String key : section.getConfigurationSection("economies").getKeys(false))
            currencies.put(FishingPlugin.getInstance().getEconomyHook().getEconomy(key), section.getDouble("economies." + key));

        final ItemStack display = new ItemBuilder(Material.getMaterial(section.getString("item.material")), 1, (short) section.getInt("item.data"))
                .setSkull(section.getString("item.url"))
                .setDisplayName(section.getString("item.name"))
                .setLore(section.getStringList("item.lore"))
                .glow(section.getBoolean("item.glow"))
                .build();

        return new Fish(id, currencies, chance, name, display);
    }
}
