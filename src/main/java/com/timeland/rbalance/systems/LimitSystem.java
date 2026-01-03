package com.timeland.rbalance.systems;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.utils.ResourceType;
import org.bukkit.entity.Player;

public class LimitSystem {
    private final RBalancePlugin plugin;

    public LimitSystem(RBalancePlugin plugin) {
        this.plugin = plugin;
    }

    public String checkDeposit(Player player, ResourceType resource, double amount) {
        if (plugin.getConfigManager().getDisabledResources().contains(resource)) {
            return "Этот ресурс отключен для депозита.";
        }

        if (amount < plugin.getConfigManager().getMinOperationAmount()) {
            return "Минимальная сумма операции: " + plugin.getConfigManager().getMinOperationAmount();
        }

        if (resource == ResourceType.NETHERITE) {
            if (player.getLevel() < plugin.getConfigManager().getNetheriteLevel()) {
                return "Вам нужен " + plugin.getConfigManager().getNetheriteLevel() + " уровень для операций с незеритом.";
            }
        }

        double daily = plugin.getDataManager().getDailyDeposit(player.getUniqueId(), resource);
        if (daily + amount > plugin.getConfigManager().getDailyLimit()) {
            return "Превышен суточный лимит депозита для этого ресурса (" + plugin.getConfigManager().getDailyLimit() + ").";
        }

        return null; // All good
    }
    
    public String checkWithdraw(Player player, ResourceType resource, double amount) {
        if (amount < plugin.getConfigManager().getMinOperationAmount()) {
            return "Минимальная сумма операции: " + plugin.getConfigManager().getMinOperationAmount();
        }

        if (resource == ResourceType.NETHERITE) {
            if (player.getLevel() < plugin.getConfigManager().getNetheriteLevel()) {
                return "Вам нужен " + plugin.getConfigManager().getNetheriteLevel() + " уровень для операций с незеритом.";
            }
        }

        return null;
    }
}
