package com.minecraftsolutions.fishing.model.bucket;

import com.minecraftsolutions.economy.model.currency.Currency;
import com.minecraftsolutions.fishing.FishingPlugin;
import com.minecraftsolutions.fishing.hook.EconomyAPI;
import com.minecraftsolutions.fishing.model.bucket.service.BucketFoundationService;
import com.minecraftsolutions.fishing.model.fish.Fish;
import com.minecraftsolutions.fishing.util.BukkitUtils;
import com.minecraftsolutions.fishing.util.item.ItemBuilder;
import com.minecraftsolutions.fishing.util.item.ItemNBT;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@AllArgsConstructor
@Data
public class Bucket {

    public final static int SLOT = FishingPlugin.getInstance().getBucket().getConfig().getInt("slot");
    public final static int CAPACITY_PER_LEVEL = FishingPlugin.getInstance().getBucket().getConfig().getInt("capacity-per-level");
    public final static int PRICE_PER_LEVEL = FishingPlugin.getInstance().getBucket().getConfig().getInt("price-per-level");
    public final static int MAX_LEVEL = FishingPlugin.getInstance().getBucket().getConfig().getInt("max-level");
    public final static EconomyAPI CURRENCY = FishingPlugin.getInstance().getEconomyHook().getEconomy(FishingPlugin.getInstance().getBucket().getConfig().getString("economy"));

    private final BucketFoundationService bucketService = FishingPlugin.getInstance().getBucketService();
    private final FileConfiguration config = FishingPlugin.getInstance().getBucket().getConfig();
    private final Map<Fish, Integer> fishes = new HashMap<>();
    private final UUID uniqueId;
    private int level;

    public Bucket(UUID uniqueId){
        this(uniqueId, 1);
    }

    public ItemStack getItem(){

        ItemStack item = new ItemBuilder(Material.getMaterial(config.getString("item.material")), 1, (short) config.getInt("item.data"))
                .setSkull(config.getString("item.url"))
                .setDisplayName(config.getString("item.name").replace("{progress_bar}", getProgressBar()).replace("{progress_raw}", String.format("%.2f", getProgressRaw() * 100)))
                .setLore(config.getStringList("item.lore"))
                .updateLore("{fishes}", getFormattedFishes())
                .build();

        ItemNBT.setBoolean(item, "fishing-bucket", true);

        return item;
    }

    public String getProgressBar(){
        return BukkitUtils.generateProgressBar(10, getProgressRaw(), '❚');
    }

    public double getProgressRaw(){
        return (double) getCapacity() / getMaxCapacity();
    }

    public int getCapacity(){
        return this.fishes.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    public int getMaxCapacity(){
        return this.level * CAPACITY_PER_LEVEL;
    }

    public double getPrice(){
        return this.level * PRICE_PER_LEVEL;
    }

    public Map<EconomyAPI, Double> getFishesPrice(){

        Map<EconomyAPI, Double> map = new HashMap<>();

        this.fishes.forEach( (fish, amount) -> {

            fish.getCurrencies().forEach( (currency, price) -> {

                map.put(currency, map.getOrDefault(currency, 0.0) + amount * price);

            });

        });

        return map;
    }

    public boolean hasEarnsToReceive(){
        return getFishesPrice().values()
                .stream()
                .anyMatch(price -> price > 0);
    }

    public int getFishesCount(){
        return this.fishes
                .values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    public void resetFishes(){
        this.fishes.keySet().forEach(fish -> setFish(fish, 0));
    }

    public void setFish(Fish fish, int amount){

        if (amount < 0)
            throw new IllegalArgumentException("Amount can't be lower than 0");

        if (amount > getMaxCapacity())
            throw new IllegalArgumentException("Maximum capacity exceeded");

        this.fishes.put(fish, amount);
        bucketService.update(this);
    }

    public int getFishAmount(Fish fish){
        return this.fishes.getOrDefault(fish, 0);
    }

    public void addFish(Fish fish, int amount) {
        int amountCanBeAdded = getMaxCapacity() - getCapacity();
        int amoutToAdd = Math.min(amountCanBeAdded, amount);
        setFish(fish, this.fishes.getOrDefault(fish, 0) + amoutToAdd);
    }

    public void removeFish(Fish fish, int amount){
        setFish(fish, this.fishes.getOrDefault(fish, 0) - amount);
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.uniqueId);
    }

    @Override
    public boolean equals(Object o){

        if (this == o)
            return true;

        if (!(o instanceof Bucket))
            return false;

        Bucket bucket = (Bucket) o;
        return this.uniqueId.equals(bucket.uniqueId);
    }

    private List<String> getFormattedFishes(){

        List<String> lines = new ArrayList<>();

        this.fishes.forEach( (fish, amount) -> {
            if (amount > 0)
                lines.add(config.getString("item.fish-format").replace("{name}", fish.getName()).replace("{amount}", FishingPlugin.FORMATTER.formatNumber(amount)));
        });

        return lines;
    }

}
