package com.minecraftsolutions.fishing.model.user;

import com.minecraftsolutions.fishing.model.booster.Booster;
import com.minecraftsolutions.fishing.model.bucket.Bucket;
import com.minecraftsolutions.fishing.model.fishingrod.FishingRod;
import com.minecraftsolutions.fishing.model.user.fishing.FishingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
@Data
public class User {

    private final Map<Booster, Integer> boosters = new HashMap<>();
    private final String nickname;
    private final FishingRod fishingRod;
    private final Bucket bucket;
    private int hookedFish;
    private int fishingTime;
    private FishingType fishingType;
    private double time;

    public void addBooster(Booster booster, int time) throws Exception {

        if (this.boosters.size() >= 4)
            throw new Exception("You can't add more than 4 booster.");

        this.boosters.put(booster, this.boosters.getOrDefault(booster, 0) + time * 20);
    }

    public void removeBooster(Booster booster){
        this.boosters.remove(booster);
    }

    public double getBoosterMultiplier(){
        return this.boosters
                .keySet()
                .stream()
                .mapToDouble(Booster::getMultiplier)
                .sum();
    }

    public int getBoosterTime(Booster booster){
        return this.boosters.getOrDefault(booster, 0);
    }

    public void addHookedFish(int amount){
        setHookedFish(getHookedFish() + amount);
    }

    public void addFishingTime(int ticks){
        setFishingTime(getFishingTime() + ticks);
    }

    public Player getPlayer(){
        return Bukkit.getPlayerExact(this.nickname);
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.nickname);
    }

    @Override
    public boolean equals(Object o){

        if (this == o)
            return true;

        if (!(o instanceof User))
            return false;

        User user = (User) o;
        return this.nickname.equals(user.nickname);
    }

}
