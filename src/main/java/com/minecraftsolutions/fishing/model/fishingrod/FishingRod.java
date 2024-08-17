package com.minecraftsolutions.fishing.model.fishingrod;

import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.hook.EconomyAPI;
import com.minecraftsolutions.fishing.model.enchant.Enchant;
import com.minecraftsolutions.fishing.model.fishingrod.slot.Slot;
import com.minecraftsolutions.fishing.util.item.ItemBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Data
public class FishingRod {

    public final static EconomyAPI SLOT_CURRENCY = FishingPlugin.getInstance().getEconomyHook().getEconomy(FishingPlugin.getInstance().getFishingRod().getConfig().getString("slot.currency"));
    public final static double SLOT_PRICE = FishingPlugin.getInstance().getFishingRod().getConfig().getDouble("slot.price");
    public final static int SLOT = FishingPlugin.getInstance().getFishingRod().getConfig().getInt("item.slot");
    public final static double DEFAULT_TIME = FishingPlugin.getInstance().getFishingRod().getConfig().getInt("default_time") * 20;

    private final FileConfiguration config = FishingPlugin.getInstance().getFishingRod().getConfig();

    private final Map<Enchant, Double> enchants = new HashMap<>(); //enchant : bonus
    private final Map<Enchant, Integer> enchantsLevel = new HashMap<>();
    private final UUID uniqueId;
    private final Set<Slot> slots;

    public FishingRod(UUID uniqueId){
        this.uniqueId = uniqueId;
        this.slots = new TreeSet<>();

        for (int i = 1; i <= 4; i++)
            this.slots.add(new Slot(i, null, false));
    }

    public ItemStack getItem(){
        return new ItemBuilder(Material.getMaterial(config.getString("item.material")), 1, (short) config.getInt("item.data"))
                .setDisplayName(config.getString("item.name"))
                .setLore(config.getStringList("item.lore"))
                .glow(config.getBoolean("item.glow"))
                .updateLore("{enchants}", getEnchantsFormatted())
                .unbreakable(true)
                .build();
    }

    public Slot getNextSlot(){
        return this.slots
                .stream()
                .filter(slot -> !slot.isBought())
                .findFirst()
                .orElse(null);
    }

    public boolean hasSlotToBuy(){
        return getNextSlot() != null;
    }

    public Slot getSlot(int position){
        return this.slots
                .stream()
                .filter(slot -> slot.getPosition() == position)
                .findFirst().orElse(null);
    }

    public Slot getSlotEnchant(Enchant enchant){
        return this.slots
                .stream()
                .filter(slot -> slot.hasEnchant() && slot.getEnchant().equals(enchant))
                .findFirst().orElse(null);
    }

    public boolean hasEnchantApplied(Enchant enchant){
        return getSlotEnchant(enchant) != null;
    }

    public void setBonus(Enchant enchant, Double bonus){
        this.enchants.put(enchant, bonus);
    }

    public void addEnchant(Enchant enchant, int level){
        this.enchantsLevel.put(enchant, level);
    }

    public int getEnchantLevel(Enchant enchant){
        return this.enchantsLevel.getOrDefault(enchant, 0);
    }

    public List<Enchant> getEnchantsInUse(){
        return this.slots
                .stream()
                .filter(Slot::hasEnchant)
                .map(Slot::getEnchant)
                .collect(Collectors.toList());
    }

    private List<String> getEnchantsFormatted(){

        List<String> lines = new ArrayList<>();

        for (Enchant enchant : getEnchantsInUse())
            lines.add(config.getString("item.enchant-format").replace("{name}", enchant.getName()).replace("{level}", FishingPlugin.FORMATTER.formatNumber(getEnchantLevel(enchant))));

        return lines;
    }

}
