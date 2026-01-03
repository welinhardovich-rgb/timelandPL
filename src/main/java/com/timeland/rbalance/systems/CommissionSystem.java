package com.timeland.rbalance.systems;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.utils.ResourceType;

import java.math.BigDecimal;

public class CommissionSystem {
    private final RBalancePlugin plugin;

    public CommissionSystem(RBalancePlugin plugin) {
        this.plugin = plugin;
    }

    public BigDecimal calculateCommission(BigDecimal amount, String type) {
        double rate = plugin.getConfigManager().getCommission(type);
        return amount.multiply(BigDecimal.valueOf(rate)).divide(BigDecimal.valueOf(100.0), 2, java.math.RoundingMode.HALF_UP);
    }

    public void applyBurn(ResourceType resource, BigDecimal burnedAmount) {
        if (burnedAmount.compareTo(BigDecimal.ZERO) <= 0) return;
        
        String path = "stats.server_burned." + resource.name();
        String currentStr = plugin.getConfig().getString(path, "0.0");
        BigDecimal current = new BigDecimal(currentStr);
        plugin.getConfig().set(path, current.add(burnedAmount).toString());
        plugin.saveConfig();
    }
}
