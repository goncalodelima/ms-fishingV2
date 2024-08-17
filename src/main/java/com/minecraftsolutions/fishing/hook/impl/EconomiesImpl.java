package com.minecraftsolutions.fishing.hook.impl;

import com.minecraftsolutions.economy.model.currency.Currency;
import com.minecraftsolutions.economy.model.user.User;
import com.minecraftsolutions.fishing.hook.EconomyAPI;
import lombok.Data;
import org.bukkit.OfflinePlayer;

@Data
public class EconomiesImpl implements EconomyAPI {

    private final com.minecraftsolutions.economy.api.EconomyAPI api;
    private final String name;

    public EconomiesImpl(String name) {

        try {
            Class.forName("com.minecraftsolutions.economy.api.EconomyAPI");
            api = new com.minecraftsolutions.economy.api.EconomyAPI();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        this.name = name;
    }

    @Override
    public double get(OfflinePlayer player) {

        Currency currency = api.getCurrency(name).orElseThrow(RuntimeException::new);
        User user = api.getUser(player.getName()).orElseThrow(RuntimeException::new);

        return api.get(currency, user);
    }

    @Override
    public void give(OfflinePlayer player, double amount) {

        Currency currency = api.getCurrency(name).orElseThrow(RuntimeException::new);
        User user = api.getUser(player.getName()).orElseThrow(RuntimeException::new);

        api.add(currency, user, amount);
    }

    @Override
    public void remove(OfflinePlayer player, double amount) {

        Currency currency = api.getCurrency(name).orElseThrow(RuntimeException::new);
        User user = api.getUser(player.getName()).orElseThrow(RuntimeException::new);

        api.remove(currency, user, amount);
    }

    @Override
    public String getIcon() {
        Currency currency = api.getCurrency(name).orElseThrow(RuntimeException::new);

        return currency.getIcon();
    }

    @Override
    public String getName() {
        Currency currency = api.getCurrency(name).orElseThrow(RuntimeException::new);

        return currency.getName();
    }

}
