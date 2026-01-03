package com.timeland.rbalance.systems;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.utils.ResourceType;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BalanceSystem {
    private final RBalancePlugin plugin;

    public BalanceSystem(RBalancePlugin plugin) {
        this.plugin = plugin;
    }

    public double getBalance(UUID uuid, ResourceType type) {
        return plugin.getDataManager().getBalance(uuid, type);
    }

    public void addBalance(UUID uuid, ResourceType type, double amount) {
        double current = getBalance(uuid, type);
        plugin.getDataManager().setBalance(uuid, type, current + amount);
    }

    public boolean withdrawBalance(UUID uuid, ResourceType type, double amount) {
        double current = getBalance(uuid, type);
        if (current < amount) {
            return false;
        }
        plugin.getDataManager().setBalance(uuid, type, current - amount);
        return true;
    }
}
