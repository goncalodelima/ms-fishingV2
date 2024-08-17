package com.minecraftsolutions.fishing.model.booster.adapter;

import com.minecraftsolutions.fishing.model.booster.Booster;
import com.minecraftsolutions.fishing.util.configuration.ConfigurationAdapter;
import com.minecraftsolutions.fishing.util.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class BoosterAdapter implements ConfigurationAdapter<Booster> {

    @Override
    public Booster adapt(ConfigurationSection section) {

        final String id = section.getName();
        final double multiplier = section.getDouble("multiplier");
        final int defaultTime = section.getInt("time");
        final ItemStack display = new ItemBuilder(Material.getMaterial(section.getString("item.material")), 1, (short) section.getInt("item.data"))
                .setSkull(section.getString("item.url"))
                .setDisplayName(section.getString("item.name"))
                .setLore(section.getStringList("item.lore"))
                .glow(section.getBoolean("item.glow"))
                .build();

        return new Booster(id, multiplier, defaultTime, display);
    }

}
