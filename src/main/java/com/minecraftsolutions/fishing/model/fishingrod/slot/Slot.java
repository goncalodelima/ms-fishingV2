package com.minecraftsolutions.fishing.model.fishingrod.slot;

import com.minecraftsolutions.fishing.model.enchant.Enchant;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@AllArgsConstructor
@Data
public class Slot implements Comparable<Slot> {

    private final int position;
    private Enchant enchant;
    private boolean bought;

    public boolean hasEnchant(){
        return this.enchant != null;
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.position);
    }

    @Override
    public boolean equals(Object o){

        if (this == o)
            return true;

        if (!(o instanceof Slot))
            return false;

        Slot slot = (Slot) o;
        return this.position == slot.position;
    }

    @Override
    public int compareTo(@NotNull Slot o) {
        return this.position - o.position;
    }
}
