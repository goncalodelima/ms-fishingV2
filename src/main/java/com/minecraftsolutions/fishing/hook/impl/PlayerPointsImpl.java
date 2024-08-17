package com.minecraftsolutions.fishing.hook.impl;

import com.minecraftsolutions.fishing.hook.EconomyAPI;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;

public class PlayerPointsImpl implements EconomyAPI {

    private PlayerPointsAPI playerPoints;

    public PlayerPointsImpl() {

        try {
            Class.forName("org.black_ixx.playerpoints.PlayerPoints");
            this.playerPoints = PlayerPoints.getInstance().getAPI();
        } catch (ClassNotFoundException ignored) {
        }

    }

    @Override
    public double get(OfflinePlayer player) {
        return playerPoints != null ? playerPoints.look(player.getUniqueId()) : 0;
    }

    @Override
    public void give(OfflinePlayer player, double amount) {
        if (playerPoints != null)
            playerPoints.give(player.getUniqueId(), (int) amount);
    }

    @Override
    public void remove(OfflinePlayer player, double amount) {
        if (playerPoints != null)
            playerPoints.take(player.getUniqueId(), (int) amount);
    }

    @Override
    public String getIcon() {
        return "§6§l✪";
    }

    @Override
    public String getName() {
        return "Cash";
    }

}
