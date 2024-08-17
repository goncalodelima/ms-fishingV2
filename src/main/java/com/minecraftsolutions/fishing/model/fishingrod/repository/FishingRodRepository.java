package com.minecraftsolutions.fishing.model.fishingrod.repository;

import com.minecraftsolutions.database.executor.DatabaseExecutor;
import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.model.enchant.Enchant;
import com.minecraftsolutions.fishing.model.enchant.service.EnchantFoundationService;
import com.minecraftsolutions.fishing.model.enchant.type.EnchantType;
import com.minecraftsolutions.fishing.model.fishingrod.FishingRod;
import com.minecraftsolutions.fishing.model.fishingrod.adapter.FishingRodAdapter;
import lombok.AllArgsConstructor;
import com.minecraftsolutions.database.Database;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class FishingRodRepository implements FishingRodFoundationRepository {

    private final Database database;
    private final FishingRodAdapter adapter = new FishingRodAdapter();
    private final EnchantFoundationService enchantService = FishingPlugin.getInstance().getEnchantService();

    @Override
    public void setup() {
        try (DatabaseExecutor executor = database.execute()) {
            executor
                    .query("CREATE TABLE IF NOT EXISTS fishing_rod (uniqueId CHAR(36), position INTEGER, enchant VARCHAR(36), bought BOOLEAN, PRIMARY KEY(uniqueId, position))")
                    .write();

            executor
                    .query("CREATE TABLE IF NOT EXISTS fishing_rod_enchant (uniqueId CHAR(36), enchant VARCHAR(36), level INTEGER, PRIMARY KEY(uniqueId, enchant))")
                    .write();
        }
    }

    @Override
    public void insert(FishingRod fishingRod) {
        try (DatabaseExecutor executor = database.execute()) {
            executor.query("INSERT INTO fishing_rod VALUES(?,?,?,?)")
                    .batch(fishingRod.getSlots(), (slot, statement) -> {
                        statement.set(1, fishingRod.getUniqueId().toString());
                        statement.set(2, slot.getPosition());
                        statement.set(3, slot.getEnchant() == null ? null : slot.getEnchant().getType().toString());
                        statement.set(4, slot.isBought());
                    });
        }
    }

    @Override
    public void update(Collection<FishingRod> fishingRods) {
        try (DatabaseExecutor executor = database.execute()) {
            executor
                    .query("UPDATE fishing_rod SET enchant = ?, bought = ? WHERE uniqueId = ? AND position = ?")
                    .batch(fishingRods.stream()
                                    .flatMap(fishingRod -> fishingRod.getSlots().stream()
                                            .map(slot -> new Object[]{slot.getEnchant() == null ? null : slot.getEnchant().getType().toString(),
                                                    slot.isBought(),
                                                    fishingRod.getUniqueId().toString(),
                                                    slot.getPosition()}))
                                    .collect(Collectors.toList()),
                            (params, statement) -> {
                                statement.set(1, params[0]);
                                statement.set(2, params[1]);
                                statement.set(3, params[2]);
                                statement.set(4, params[3]);
                            });

            executor
                    .query("INSERT INTO fishing_rod_enchant (uniqueId, enchant, level) VALUES(?,?,?) ON DUPLICATE KEY UPDATE level = VALUES(level)")
                    .batch(fishingRods.stream()
                                    .flatMap(fishingRod -> fishingRod.getEnchantsLevel().entrySet().stream()
                                            .map(entry -> new Object[]{fishingRod.getUniqueId().toString(),
                                                    entry.getKey().getType().toString(),
                                                    entry.getValue()}))
                                    .collect(Collectors.toList()),
                            (params, statement) -> {
                                statement.set(1, params[0]);
                                statement.set(2, params[1]);
                                statement.set(3, params[2]);
                            });
        }
    }

    @Override
    public FishingRod findOne(UUID uniqueId) {

        try (DatabaseExecutor executor = database.execute()) {
            FishingRod fishingRod = executor
                    .query("SELECT * FROM fishing_rod WHERE uniqueId = ?")
                    .readOne(statement -> statement.set(1, uniqueId.toString()), this.adapter).orElse(null);

            if (fishingRod != null) {
                executor
                        .query("SELECT * FROM fishing_rod_enchant WHERE uniqueId = ?")
                        .readOne(statement -> statement.set(1, uniqueId.toString()), query -> {

                            do {
                                final Enchant enchant = enchantService.get(EnchantType.valueOf((String) query.get("enchant"))).orElseThrow(RuntimeException::new);
                                final int level = (Integer) query.get("level");
                                fishingRod.addEnchant(enchant, level);
                            } while (query.next());

                            return null;
                        });
            }

            return fishingRod;
        }

    }

}
