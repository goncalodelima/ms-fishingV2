package com.minecraftsolutions.fishing.hook.impl;

import com.nextplugins.cash.NextCash;
import com.nextplugins.cash.api.model.account.Account;
import org.bukkit.OfflinePlayer;
import com.minecraftsolutions.fishing.hook.EconomyAPI;

public class NextCashImpl implements EconomyAPI {

    private NextCash nextCash;

    public NextCashImpl() {

        try {
            Class.forName("com.nextplugins.cash.NextCash");
            this.nextCash = NextCash.getInstance();
        } catch (ClassNotFoundException ignored) {

        }

    }

    @Override
    public double get(OfflinePlayer player) {
        Account account = nextCash.getAccountStorage().findAccount(player);
        return account != null ? account.getBalance() : 0;
    }

    @Override
    public void give(OfflinePlayer player, double amount) {
        Account account = nextCash.getAccountStorage().findAccount(player);
        if (account != null) account.depositAmount(amount);
    }

    @Override
    public void remove(OfflinePlayer player, double amount) {
        Account account = nextCash.getAccountStorage().findAccount(player);
        if (account != null) account.withdrawAmount(amount);
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
