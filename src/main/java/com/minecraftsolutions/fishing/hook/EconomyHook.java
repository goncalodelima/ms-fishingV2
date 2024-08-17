package com.minecraftsolutions.fishing.hook;

import com.minecraftsolutions.fishing.hook.impl.EconomiesImpl;
import com.minecraftsolutions.fishing.hook.impl.NextCashImpl;
import com.minecraftsolutions.fishing.hook.impl.PlayerPointsImpl;
import com.minecraftsolutions.fishing.hook.impl.VaultImpl;

public class EconomyHook {

    private final EconomyAPI vault;
    private final EconomyAPI playerpoints;
    private final EconomyAPI nextcash;

    public EconomyHook(){

        this.vault = new VaultImpl();
        this.playerpoints = new PlayerPointsImpl();
        this.nextcash = new NextCashImpl();

    }

    public EconomyAPI getEconomy(String economy){

        switch (economy){
            case "Vault":
                return this.vault;
            case "PlayerPoints":
                return this.playerpoints;
            case "NextCash":
                return this.nextcash;
            default:
                return new EconomiesImpl(economy);
        }

    }

}
