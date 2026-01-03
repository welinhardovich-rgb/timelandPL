package com.timeland.rbalance.storage;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.utils.ResourceType;
import org.bukkit.configuration.ConfigurationSection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    private final YamlHandler playersYaml;
    private final YamlHandler dailyDepositsYaml;
    private final Map<UUID, Map<ResourceType, BigDecimal>> balances = new HashMap<>();
    private final Map<UUID, Map<ResourceType, BigDecimal>> dailyDeposits = new HashMap<>();
    private final Map<UUID, Boolean> bossBarEnabled = new HashMap<>();
    private final Map<UUID, java.util.List<String>> history = new HashMap<>();
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
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    Map<ResourceType, BigDecimal> playerBal = new HashMap<>();
                    ConfigurationSection balSection = playersSection.getConfigurationSection(uuidStr + ".balance");
                    if (balSection != null) {
                        for (ResourceType type : ResourceType.values()) {
                            String val = balSection.getString(type.name(), "0.0");
                            playerBal.put(type, new BigDecimal(val));
                        }
                    }
                    balances.put(uuid, playerBal);
                    bossBarEnabled.put(uuid, playersSection.getBoolean(uuidStr + ".bossbar", true));
                } catch (Exception ignored) {}
            }
        }

        dailyDeposits.clear();
        lastResetDate = dailyDepositsYaml.getConfig().getString("last_reset", LocalDate.now().toString());
        checkDailyReset();
        
        ConfigurationSection depositsSection = dailyDepositsYaml.getConfig().getConfigurationSection("deposits");
        if (depositsSection != null) {
            for (String uuidStr : depositsSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    Map<ResourceType, BigDecimal> playerDeposits = new HashMap<>();
                    ConfigurationSection resSection = depositsSection.getConfigurationSection(uuidStr);
                    if (resSection != null) {
                        for (ResourceType type : ResourceType.values()) {
                            String val = resSection.getString(type.name(), "0.0");
                            playerDeposits.put(type, new BigDecimal(val));
                        }
                    }
                    dailyDeposits.put(uuid, playerDeposits);
                } catch (Exception ignored) {}
            }
        }
    }

    public void save() {
        // Save balances
        for (Map.Entry<UUID, Map<ResourceType, BigDecimal>> entry : balances.entrySet()) {
            UUID uuid = entry.getKey();
            String path = "players." + uuid.toString();
            for (Map.Entry<ResourceType, BigDecimal> balEntry : entry.getValue().entrySet()) {
                playersYaml.getConfig().set(path + ".balance." + balEntry.getKey().name(), balEntry.getValue().toString());
            }
            playersYaml.getConfig().set(path + ".bossbar", bossBarEnabled.getOrDefault(uuid, true));
        }
        playersYaml.save();

        // Save daily deposits
        dailyDepositsYaml.getConfig().set("last_reset", lastResetDate);
        for (Map.Entry<UUID, Map<ResourceType, BigDecimal>> entry : dailyDeposits.entrySet()) {
            String path = "deposits." + entry.getKey().toString();
            for (Map.Entry<ResourceType, BigDecimal> depEntry : entry.getValue().entrySet()) {
                dailyDepositsYaml.getConfig().set(path + "." + depEntry.getKey().name(), depEntry.getValue().toString());
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

    public BigDecimal getBalance(UUID uuid, ResourceType type) {
        return balances.getOrDefault(uuid, new HashMap<>()).getOrDefault(type, BigDecimal.ZERO);
    }

    public void setBalance(UUID uuid, ResourceType type, BigDecimal amount) {
        balances.computeIfAbsent(uuid, k -> new HashMap<>()).put(type, amount);
    }

    public BigDecimal getDailyDeposit(UUID uuid, ResourceType type) {
        checkDailyReset();
        return dailyDeposits.getOrDefault(uuid, new HashMap<>()).getOrDefault(type, BigDecimal.ZERO);
    }

    public void addDailyDeposit(UUID uuid, ResourceType type, BigDecimal amount) {
        checkDailyReset();
        Map<ResourceType, BigDecimal> playerDeposits = dailyDeposits.computeIfAbsent(uuid, k -> new HashMap<>());
        playerDeposits.put(type, playerDeposits.getOrDefault(type, BigDecimal.ZERO).add(amount));
    }
    
    public Map<UUID, BigDecimal> getAllBalances(ResourceType type) {
        Map<UUID, BigDecimal> result = new HashMap<>();
        for (Map.Entry<UUID, Map<ResourceType, BigDecimal>> entry : balances.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getOrDefault(type, BigDecimal.ZERO));
        }
        return result;
    }
    
    public void setPlayerName(UUID uuid, String name) {
        playersYaml.getConfig().set("players." + uuid.toString() + ".name", name);
    }

    public String getPlayerName(UUID uuid) {
        return playersYaml.getConfig().getString("players." + uuid.toString() + ".name", "Unknown");
    }

    public boolean isBossBarEnabled(UUID uuid) {
        return bossBarEnabled.getOrDefault(uuid, true);
    }

    public void setBossBarEnabled(UUID uuid, boolean enabled) {
        bossBarEnabled.put(uuid, enabled);
    }

    public void addHistory(UUID uuid, String entry) {
        java.util.List<String> userHistory = history.computeIfAbsent(uuid, k -> new java.util.ArrayList<>());
        userHistory.add(0, entry);
        if (userHistory.size() > 10) {
            userHistory.remove(10);
        }
    }

    public java.util.List<String> getHistory(UUID uuid) {
        return history.getOrDefault(uuid, java.util.Collections.emptyList());
    }
}
