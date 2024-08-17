package com.minecraftsolutions.fishing.util.item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ItemBuilder {

    private final ItemStack item;

    public ItemBuilder(ItemStack item){
        this.item = item;
    }

    public ItemBuilder(Material type){
        this.item = new ItemStack(type);
    }

    public ItemBuilder(Material type, int amount){
        this.item = new ItemStack(type, amount);
    }

    public ItemBuilder(Material type, int amount, short data){
        this.item = new ItemStack(type, amount, data);
    }

    public ItemBuilder changeItemMeta(Consumer<ItemMeta> consumer){
        ItemMeta meta = this.item.getItemMeta();
        consumer.accept(meta);
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder changeSkullMeta(Consumer<SkullMeta> consumer){
        SkullMeta meta = (SkullMeta) this.item.getItemMeta();
        consumer.accept(meta);
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setDisplayName(String name){
        return name == null ? this : changeItemMeta(meta -> meta.setDisplayName(name.replace("&", "§")));
    }

    public ItemBuilder setLore(List<String> lore){
        return lore == null ? this : changeItemMeta(meta -> meta.setLore(lore.stream().map(str -> str.replace("&", "§")).collect(Collectors.toList())));
    }

    public ItemBuilder setSkull(String url){

        if (url == null)
            return this;

        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "Notch");
        byte[] arrayOfByte = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        gameProfile.getProperties().put("textures", new Property("textures", new String(arrayOfByte)));
        Field field;
        try {
            field = skullMeta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(skullMeta, gameProfile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException | SecurityException noSuchFieldException) {
            noSuchFieldException.printStackTrace();
        }
        item.setItemMeta(skullMeta);
        return this;
    }

    public ItemBuilder addLore(List<String> lines){

        if (lines == null)
            return this;

        lines.replaceAll(s -> s.replace("&", "§"));
        return changeItemMeta(meta -> {
            List<String> lore = meta.getLore();
            lore.addAll(lines);
            meta.setLore(lore);
        });
    }

    public ItemBuilder addLore(String line){
        return addLore(Collections.singletonList(line));
    }

    public ItemBuilder updateLore(String key, List<String> lines){

        return changeItemMeta(meta -> {

            List<String> lore = new ArrayList<>();

            for (String line : Objects.requireNonNull(meta.getLore())){
                if (line.equals(key)) lore.addAll(lines.stream().map(str -> str.replace("&", "§")).collect(Collectors.toList()));
                else lore.add(line);
                }

            meta.setLore(lore);
        });
    }

    public ItemBuilder updateLore(String key, String l){
        return updateLore(key, Collections.singletonList(l));
    }

    public ItemBuilder glow(boolean b){
        return b ? addItemFlag(ItemFlag.HIDE_ENCHANTS).addEnchantment(Enchantment.ARROW_DAMAGE, 1) : this;
    }

    public ItemBuilder addItemFlag(ItemFlag... itemFlags){
        return changeItemMeta(meta -> meta.addItemFlags(itemFlags));
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level){
        return changeItemMeta(meta -> meta.addEnchant(enchantment, level, true));
    }



    public ItemBuilder unbreakable(boolean b){
        return changeItemMeta(meta -> meta.spigot().setUnbreakable(b));
    }

    public ItemStack build(){
        return this.item;
    }

}
