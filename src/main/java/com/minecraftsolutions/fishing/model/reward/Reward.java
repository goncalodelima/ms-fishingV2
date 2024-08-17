package com.minecraftsolutions.fishing.model.reward;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@AllArgsConstructor
@Data
@EqualsAndHashCode
public class Reward {

    private final String command;
    private final double chance;

    public void give(Player player){
        if (command != null)
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
    }

}
