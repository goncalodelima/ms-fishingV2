package com.minecraftsolutions.fishing.model.enchant.adapter;

import com.minecraftsolutions.economy.api.EconomyAPI;
import com.minecraftsolutions.economy.model.currency.Currency;
import com.minecraftsolutions.fishing.model.enchant.Enchant;
import com.minecraftsolutions.fishing.model.enchant.type.EnchantType;
import com.minecraftsolutions.fishing.util.configuration.ConfigurationAdapter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.stream.Collectors;

public class EnchantAdapter implements ConfigurationAdapter<Enchant> {

    private final EconomyAPI api = new EconomyAPI();

    @Override
    public Enchant adapt(ConfigurationSection section) {

        final EnchantType type = EnchantType.valueOf(section.getName());
        final Material material = Material.getMaterial(section.getString("material"));
        final short data = (short) section.getInt("data");
        final String url = section.getString("url");
        final String name = section.getString("name").replace("&", "§");
        final int maxLevel = section.getInt("maxLevel");
        final double price = section.getDouble("price");
        final double pricePerLevel = section.getDouble("pricePerLevel");
        final double bonus = section.getDouble("bonus");
        final Currency currency = api.getCurrency(section.getString("currency")).orElseThrow(RuntimeException::new);
        final List<String> description = section.getStringList("description")
                .stream()
                .map(str -> str.replace("&", "§"))
                .collect(Collectors.toList());

        return new Enchant(type, currency, material, data, url, name, description, maxLevel, price, pricePerLevel, bonus);
    }

}
