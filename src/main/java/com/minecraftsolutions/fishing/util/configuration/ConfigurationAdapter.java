package com.minecraftsolutions.fishing.util.configuration;

import org.bukkit.configuration.ConfigurationSection;

public interface ConfigurationAdapter<T> {

    T adapt(ConfigurationSection section);

}
