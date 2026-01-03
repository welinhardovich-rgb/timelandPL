package com.timeland.rbalance.storage;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.utils.ResourceType;
import org.bukkit.configuration.ConfigurationSection;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    private final YamlHandler playersYaml;
    private final YamlHandler dailyDepositsYaml;
    private final Map<UUID, Map<ResourceType, Double>> balances = new HashMap<>();
    private final Map<UUID, Map<ResourceType, Double>> dailyDeposits = new HashMap<>();
    private String lastResetDate;

    public DataManager(RBalancePlugin plugin) {
        this.playersYaml = new YamlHandler(plugin, "players.yml");
        this.dailyDepositsYaml = new YamlHandler(plugin, "daily_deposits.yml");
        load();
    }

    public void load() {
        playersYaml.reload();
        dailyDepositsYaml.reload();
        
        balances.clear();
        ConfigurationSection playersSection = playersYaml.getConfig().getConfigurationSection("players");
        if (playersSection != null) {
            for (String uuidStr : playersSection.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                Map<ResourceType, Double> playerBal = new HashMap<>();
                ConfigurationSection balSection = playersSection.getConfigurationSection(uuidStr + ".balance");
                if (balSection != null) {
                    for (ResourceType type : ResourceType.values()) {
                        playerBal.put(type, balSection.getDouble(type.name(), 0.0));
                    }
                }
                balances.put(uuid, playerBal);
            }
        }

        dailyDeposits.clear();
        lastResetDate = dailyDepositsYaml.getConfig().getString("last_reset", LocalDate.now().toString());
        checkDailyReset();
        
        ConfigurationSection depositsSection = dailyDepositsYaml.getConfig().getConfigurationSection("deposits");
        if (depositsSection != null) {
            for (String uuidStr : depositsSection.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                Map<ResourceType, Double> playerDeposits = new HashMap<>();
                ConfigurationSection resSection = depositsSection.getConfigurationSection(uuidStr);
                if (resSection != null) {
                    for (ResourceType type : ResourceType.values()) {
                        playerDeposits.put(type, resSection.getDouble(type.name(), 0.0));
                    }
                }
                dailyDeposits.put(uuid, playerDeposits);
            }
        }
    }

    public void save() {
        // Save balances
        for (Map.Entry<UUID, Map<ResourceType, Double>> entry : balances.entrySet()) {
            String path = "players." + entry.getKey().toString();
            playersYaml.getConfig().set(path + ".name", ""); // We might want to store names too if needed
            for (Map.Entry<ResourceType, Double> balEntry : entry.getValue().entrySet()) {
                playersYaml.getConfig().set(path + ".balance." + balEntry.getKey().name(), balEntry.getValue());
            }
        }
        playersYaml.save();

        // Save daily deposits
        dailyDepositsYaml.getConfig().set("last_reset", lastResetDate);
        for (Map.Entry<UUID, Map<ResourceType, Double>> entry : dailyDeposits.entrySet()) {
            String path = "deposits." + entry.getKey().toString();
            for (Map.Entry<ResourceType, Double> depEntry : entry.getValue().entrySet()) {
                dailyDepositsYaml.getConfig().set(path + "." + depEntry.getKey().name(), depEntry.getValue());
            }
        }
        dailyDepositsYaml.save();
    }

    private void checkDailyReset() {
        String today = LocalDate.now().toString();
        if (!today.equals(lastResetDate)) {
            dailyDeposits.clear();
            lastResetDate = today;
            dailyDepositsYaml.getConfig().set("deposits", null);
            dailyDepositsYaml.save();
        }
    }

    public double getBalance(UUID uuid, ResourceType type) {
        return balances.getOrDefault(uuid, new HashMap<>()).getOrDefault(type, 0.0);
    }

    public void setBalance(UUID uuid, ResourceType type, double amount) {
        balances.computeIfAbsent(uuid, k -> new HashMap<>()).put(type, amount);
    }

    public double getDailyDeposit(UUID uuid, ResourceType type) {
        checkDailyReset();
        return dailyDeposits.getOrDefault(uuid, new HashMap<>()).getOrDefault(type, 0.0);
    }

    public void addDailyDeposit(UUID uuid, ResourceType type, double amount) {
        checkDailyReset();
        Map<ResourceType, Double> playerDeposits = dailyDeposits.computeIfAbsent(uuid, k -> new HashMap<>());
        playerDeposits.put(type, playerDeposits.getOrDefault(type, 0.0) + amount);
    }
    
    public Map<UUID, Double> getAllBalances(ResourceType type) {
        Map<UUID, Double> result = new HashMap<>();
        for (Map.Entry<UUID, Map<ResourceType, Double>> entry : balances.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getOrDefault(type, 0.0));
        }
        return result;
    }
    
    public void setPlayerName(UUID uuid, String name) {
        playersYaml.getConfig().set("players." + uuid.toString() + ".name", name);
    }

    public String getPlayerName(UUID uuid) {
        return playersYaml.getConfig().getString("players." + uuid.toString() + ".name", "Unknown");
    }
}
