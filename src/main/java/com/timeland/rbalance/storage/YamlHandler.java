package com.timeland.rbalance.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class YamlHandler {
    private final JavaPlugin plugin;
    private final String fileName;
    private final File file;
    private FileConfiguration config;

    public YamlHandler(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.file = new File(plugin.getDataFolder(), fileName);
    }

    public void reload() {
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reload();
        }
        return config;
    }

    public void save() {
        if (config == null || file == null) {
            return;
        }
        try {
            getConfig().save(file);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + file, ex);
        }
    }
}
