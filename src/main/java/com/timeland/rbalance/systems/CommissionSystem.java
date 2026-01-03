package com.timeland.rbalance.systems;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.utils.ResourceType;

public class CommissionSystem {
    private final RBalancePlugin plugin;

    public CommissionSystem(RBalancePlugin plugin) {
        this.plugin = plugin;
    }

    public double calculateCommission(double amount, String type) {
        double rate = plugin.getConfigManager().getCommission(type);
        return amount * (rate / 100.0);
    }

    public void applyBurn(ResourceType resource, double burnedAmount) {
        if (burnedAmount <= 0) return;
        
        String path = "stats.server_burned." + resource.name();
        double current = plugin.getConfig().getDouble(path, 0.0);
        plugin.getConfig().set(path, current + burnedAmount);
        plugin.saveConfig();
    }
}
