package com.minecraftsolutions.fishing.util.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Configuration {

    private File file;
    private String name;
    private JavaPlugin javaPlugin;

    private FileConfiguration configuration;

    public Configuration(String name, JavaPlugin javaPlugin) {
        this.name = name;
        this.javaPlugin = javaPlugin;
    }

    public Configuration(File file) {
        this.file = file;
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    public void saveFile() {
        javaPlugin.saveResource(name, false);
        file = new File(this.javaPlugin.getDataFolder() + File.separator + name);
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    public void saveConfig() {
        if (configuration != null) {
            File configFile = this.file;
            try {
                configuration.save(configFile);
            } catch (IOException ignored) {

            }
        }
    }

    public FileConfiguration getConfig() {
        return configuration;
    }

}
