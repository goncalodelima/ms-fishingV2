package com.minecraftsolutions.fishing.model.booster;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

@AllArgsConstructor
@Data
public class Booster {

    private final String id;
    private final double multiplier;
    private final int defaultTime;
    private final ItemStack display;

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if (!(o instanceof Booster))
            return false;

        Booster booster = (Booster) o;
        return this.id.equals(booster.id);
    }

}
