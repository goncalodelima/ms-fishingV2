package com.minecraftsolutions.fishing.model.fish;

import com.minecraftsolutions.fishing.hook.EconomyAPI;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
@Data
public class Fish {

    private final String id;
    private final Map<EconomyAPI, Double> currencies;
    private final double chance;
    private final String name;
    private final ItemStack display;

    @Override
    public int hashCode(){
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(Object o){

        if (this == o)
            return true;

        if (!(o instanceof Fish))
            return false;

        Fish fish = (Fish) o;
        return this.id.equals(fish.id);
    }

}
