package com.timeland.rbalance.systems;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.api.events.BalanceChangeEvent;
import com.timeland.rbalance.utils.ResourceType;
import org.bukkit.Bukkit;

import java.math.BigDecimal;
import java.util.UUID;

public class BalanceSystem {
    private final RBalancePlugin plugin;

    public BalanceSystem(RBalancePlugin plugin) {
        this.plugin = plugin;
    }

    public BigDecimal getBalance(UUID uuid, ResourceType type) {
        return plugin.getDataManager().getBalance(uuid, type);
    }

    public void addBalance(UUID uuid, ResourceType type, BigDecimal amount) {
        BigDecimal current = getBalance(uuid, type);
        BigDecimal next = current.add(amount);
        
        BalanceChangeEvent event = new BalanceChangeEvent(uuid, type, current, next);
        Bukkit.getPluginManager().callEvent(event);
        
        if (!event.isCancelled()) {
            plugin.getDataManager().setBalance(uuid, type, event.getNewBalance());
        }
    }

    public boolean withdrawBalance(UUID uuid, ResourceType type, BigDecimal amount) {
        BigDecimal current = getBalance(uuid, type);
        if (current.compareTo(amount) < 0) {
            return false;
        }
        
        BigDecimal next = current.subtract(amount);
        BalanceChangeEvent event = new BalanceChangeEvent(uuid, type, current, next);
        Bukkit.getPluginManager().callEvent(event);
        
        if (!event.isCancelled()) {
            plugin.getDataManager().setBalance(uuid, type, event.getNewBalance());
            return true;
        }
        return false;
    }

    public void setBalance(UUID uuid, ResourceType type, BigDecimal amount) {
        BigDecimal current = getBalance(uuid, type);
        BalanceChangeEvent event = new BalanceChangeEvent(uuid, type, current, amount);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            plugin.getDataManager().setBalance(uuid, type, event.getNewBalance());
        }
    }
}
