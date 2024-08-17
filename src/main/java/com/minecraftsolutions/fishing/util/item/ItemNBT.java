package com.minecraftsolutions.fishing.util.item;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class ItemNBT {

    private static void consume(@NotNull ItemStack itemStack, @NotNull Consumer<NBTTagCompound> consumer) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound itemCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
        consumer.accept(itemCompound);
        nmsItem.setTag(itemCompound);
        itemStack.setItemMeta(CraftItemStack.getItemMeta(nmsItem));
    }

    private static <T> T supply(@NotNull ItemStack itemStack, @NotNull Function<NBTTagCompound, T> function) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound itemCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
        return function.apply(itemCompound);
    }

    public static boolean hasTag(ItemStack item, String tag){
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsItem.getTag();
        return compound != null && compound.hasKey(tag);
    }

    public static String getString(ItemStack stack, String key) {
        return supply(stack, nbtTagCompound -> nbtTagCompound.getString(key));
    }

    public static long getLong(ItemStack stack, String key) {
        return supply(stack, compound -> compound.getLong(key));
    }

    public static int getInt(ItemStack stack, String key) {
        return supply(stack, compound -> compound.getInt(key));
    }

    public static double getDouble(ItemStack stack, String key) {
        return supply(stack, nbtTagCompound -> nbtTagCompound.getDouble(key));
    }

    public static boolean getBoolean(ItemStack stack, String key) {
        return supply(stack, nbtTagCompound -> nbtTagCompound.getBoolean(key));
    }

    public static byte getByte(ItemStack stack, String key) {
        return supply(stack, nbtTagCompound -> nbtTagCompound.getByte(key));
    }

    public static float getFloat(ItemStack stack, String key) {
        return supply(stack, nbtTagCompound -> nbtTagCompound.getFloat(key));
    }

    public static short getShort(ItemStack stack, String key) {
        return supply(stack, nbtTagCompound -> nbtTagCompound.getShort(key));
    }

    public static void setLong(ItemStack stack, String key, long value) {
        consume(stack, compound -> compound.setLong(key, value));
    }

    public static void setString(ItemStack stack, String key, String value) {
        consume(stack, compound -> compound.setString(key, value));
    }

    public static void setDouble(ItemStack stack, String key, double value) {
        consume(stack, compound -> compound.setDouble(key, value));
    }

    public static void setInt(ItemStack stack, String key, int value) {
        consume(stack, compound -> compound.setInt(key, value));
    }

    public static void setBoolean(ItemStack stack, String key, boolean value) {
        consume(stack, compound -> compound.setBoolean(key, value));
    }

    public static void setByte(ItemStack stack, String key, byte value) {
        consume(stack, compound -> compound.setByte(key, value));
    }

    public static void setFloat(ItemStack stack, String key, float value) {
        consume(stack, compound -> compound.setFloat(key, value));
    }

    public static void setShort(ItemStack stack, String key, short value) {
        consume(stack, compound -> compound.setShort(key, value));
    }
}
