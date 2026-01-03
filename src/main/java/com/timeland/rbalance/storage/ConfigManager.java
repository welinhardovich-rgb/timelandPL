package com.timeland.rbalance.storage;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.utils.ResourceType;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {
    private final RBalancePlugin plugin;
    private FileConfiguration config;

    public ConfigManager(RBalancePlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public double getCommission(String type) {
        return config.getDouble("commissions." + type, 0.0);
    }

    public double getDailyLimit() {
        return config.getDouble("limits.daily_deposit_per_resource", 1000.0);
    }

    public double getMinOperationAmount() {
        return config.getDouble("limits.min_operation_amount", 0.1);
    }

    public int getNetheriteUnlockLevel() {
        return config.getIntegerList("limits.netherite_unlock_level").isEmpty() ? 
                config.getInt("limits.netherite_unlock_level", 30) : 30;
    }
    
    // Fixed logic for Netherite level
    public int getNetheriteLevel() {
        return config.getInt("limits.netherite_unlock_level", 30);
    }

    public List<ResourceType> getDisabledResources() {
        return config.getStringList("limits.disabled_resources").stream()
                .map(ResourceType::fromString)
                .filter(res -> res != null)
                .collect(Collectors.toList());
    }
}
