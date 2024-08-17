package com.minecraftsolutions.fishing.model.fishingrod.adapter;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.model.enchant.Enchant;
import com.minecraftsolutions.fishing.model.enchant.service.EnchantFoundationService;
import com.minecraftsolutions.fishing.model.enchant.type.EnchantType;
import com.minecraftsolutions.fishing.model.fishingrod.FishingRod;
import com.minecraftsolutions.fishing.model.fishingrod.slot.Slot;
import com.minecraftsolutions.database.adapter.DatabaseAdapter;
import com.minecraftsolutions.database.executor.DatabaseQuery;

import java.util.*;

public class FishingRodAdapter implements DatabaseAdapter<FishingRod> {

    private final EnchantFoundationService enchantService = FishingPlugin.getInstance().getEnchantService();

    @Override
    public FishingRod adapt(DatabaseQuery databaseQuery) {

        final UUID uniqueId = UUID.fromString((String) databaseQuery.get("uniqueId"));
        final Set<Slot> slots = new TreeSet<>();

        do {

            final int position = (Integer) databaseQuery.get("position");
            final boolean bought = (Boolean) databaseQuery.get("bought");
            final Enchant enchant = databaseQuery.get("enchant") == null ? null : enchantService.get(EnchantType.valueOf((String) databaseQuery.get("enchant"))).orElseThrow(RuntimeException::new);
            final Slot slot = new Slot(position, enchant, bought);
            slots.add(slot);

        }while (databaseQuery.next());

        return new FishingRod(uniqueId, slots);
    }

}
