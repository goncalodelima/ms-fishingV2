package com.minecraftsolutions.fishing.model.enchant;

import com.minecraftsolutions.economy.model.currency.Currency;
import com.minecraftsolutions.fishing.model.enchant.type.EnchantType;
import com.minecraftsolutions.fishing.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Material;

import java.util.List;

@AllArgsConstructor
@Data
public class Enchant {

    private final EnchantType type;
    private final Currency currency;
    private final Material material;
    private final short data;
    private final String url;
    private final String name;
    private final List<String> description;
    private final int maxLevel;
    private final double price;
    private final double pricePerLevel;
    private final double bonus;

    public void run(User user){
        user.getFishingRod().setBonus(this, user.getFishingRod().getEnchantLevel(this) * this.bonus);
    }

    public double getPrice(int level){
        return price + level * pricePerLevel;
    }

    @Override
    public boolean equals(Object o){

        if (this == o)
            return true;

        if (!(o instanceof Enchant))
            return false;

        Enchant enchant = (Enchant) o;
        return this.type.equals(enchant.type);
    }

}
