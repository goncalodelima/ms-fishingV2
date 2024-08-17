package com.minecraftsolutions.fishing.hook;

import org.bukkit.OfflinePlayer;

public interface EconomyAPI {

    double get(OfflinePlayer player);

    void give(OfflinePlayer player, double amount);

    void remove(OfflinePlayer player, double amount);

    String getIcon();

    String getName();

}
