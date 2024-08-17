package com.minecraftsolutions.fishing.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationSerializer {

    public static String serializeLocation(Location location){
        return location.getWorld().getName() + ";" + location.getX() + ";" + location.getY() + ";" + location.getZ() + ";" + location.getYaw() + ";" + location.getPitch();
    }

    public static Location deserializeLocation(String params){
        String[] a = params.split(";");
        return new Location(Bukkit.getWorld(a[0]), Double.parseDouble(a[1]), Double.parseDouble(a[2]), Double.parseDouble(a[3]), Float.parseFloat(a[4]), Float.parseFloat(a[5]));
    }

}
