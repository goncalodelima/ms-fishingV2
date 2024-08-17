package com.minecraftsolutions.fishing.hook.impl;

import com.minecraftsolutions.fishing.hook.EconomyAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultImpl implements EconomyAPI {

    private Economy vault;

    public VaultImpl() {

        RegisteredServiceProvider<Economy> registeredServiceProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (registeredServiceProvider == null) {
            return;
        }

        vault = registeredServiceProvider.getProvider();
    }

    @Override
    public double get(OfflinePlayer player) {
        return vault != null ? vault.getBalance(player) : 0;
    }

    @Override
    public void give(OfflinePlayer player, double amount) {
        if (vault != null)
            vault.depositPlayer(player, amount);
    }

    @Override
    public void remove(OfflinePlayer player, double amount) {
        if (vault != null)
            vault.withdrawPlayer(player, amount);
    }

    @Override
    public String getIcon() {
        return "§2$";
    }

    @Override
    public String getName() {
        return "Coins";
    }

}
