package com.minecraftsolutions.fishing.util;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.function.Supplier;

public class BukkitUtils {

    public static void sendClickableCommand(Player player, String message, String command, String hover) {

        TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));

        if (hover != null)
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hover)).create()));

        player.spigot().sendMessage(component);
    }

    public static <T> T randomObject(Map<T, Double> objects){

        double chanceSum = objects.values().stream().reduce(0.0, Double::sum);
        double random = new Random().nextDouble() * chanceSum;
        double accumulator = 0.0;

        for (Map.Entry<T, Double> entry : objects.entrySet()){
            accumulator += entry.getValue();
            if (random < accumulator) return entry.getKey();
        }

        return null;
    }

    public static void sendActionBar(Player player, String message) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message.replace("&", "§")), (byte) 2);
        craftPlayer.getHandle().playerConnection.sendPacket(packet);
    }

    public static String generateProgressBar(int barSize, double percentage, char character) {

        percentage = percentage < 0 ? 0 : percentage > 1 ? 1 : percentage;
        int progressChars = (int) Math.floor(percentage * barSize);
        int emptyChars = barSize - progressChars;

        StringBuilder progressBar = new StringBuilder();

        for (int i = 0; i < progressChars; i++)
            progressBar.append("§a").append(character);

        for (int i = 0; i < emptyChars; i++)
            progressBar.append("§7").append(character);

        return progressBar.toString();
    }

    public static Set<Block> getArea(Location location1, Location location2){

        Set<Block> blocks = new HashSet<>();
        int xMin = Math.min(location1.getBlockX(), location2.getBlockX());
        int yMin = Math.min(location1.getBlockY(), location2.getBlockY());
        int zMin = Math.min(location1.getBlockZ(), location2.getBlockZ());
        int xMax = Math.max(location1.getBlockX(), location2.getBlockX());
        int yMax = Math.max(location1.getBlockY(), location2.getBlockY());
        int zMax = Math.max(location1.getBlockZ(), location2.getBlockZ());

        for (int x = xMin; x <= xMax; x++)
            for (int y = yMin; y <= yMax; y++)
                for (int z = zMin; z <= zMax; z++)
                    blocks.add(location1.getWorld().getBlockAt(new Location(location1.getWorld(), x, y, z)));

        return blocks;
    }

    public static int getInventorySpace(Inventory inventory){
        return (int) Arrays.stream(inventory.getContents()).filter(Objects::isNull).count();
    }

    public static boolean isInventoryFull(Inventory inventory){
        return getInventorySpace(inventory) <= 0;
    }

    public static String formatSeconds(int seconds){

        if(seconds < 0) {
            throw new IllegalArgumentException("Seconds can't be negative.");
        }

        int days = seconds / (24 * 3600);
        seconds = seconds % (24 * 3600);
        int hours = seconds / 3600;
        seconds %= 3600;
        int minutes = seconds / 60 ;
        seconds %= 60;

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append("d").append(hours > 0 || minutes > 0 || seconds > 0 ? ", " : "");
        }
        if (hours > 0) {
            result.append(hours).append("h").append(minutes > 0 || seconds > 0 ? ", " : "");
        }
        if (minutes > 0) {
            result.append(minutes).append("m").append(seconds > 0 ? ", " : "");
        }
        if (seconds > 0 || result.length() == 0) {
            result.append(seconds).append("s");
        }

        return result.toString();
    }

    public static void sendTitle(Player player, String titleText, String subTitleText, int fadeIn, int stay, int fadeOut){
        IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + titleText.replace("&", "§") + "\",color:" + ChatColor.GOLD.name().toLowerCase() + "}");
        IChatBaseComponent chatSubTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + subTitleText.replace("&", "§") + "\",color:" + ChatColor.GOLD.name().toLowerCase() + "}");

        PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, chatTitle);
        PacketPlayOutTitle subTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, chatSubTitle);
        PacketPlayOutTitle length = new PacketPlayOutTitle(fadeIn, stay, fadeOut);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(subTitle);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(length);

    }

}
