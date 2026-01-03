package com.timeland.rbalance.events;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.api.events.BalanceChangeEvent;
import com.timeland.rbalance.api.events.SignTradeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class EconomyListener implements Listener {
    private final RBalancePlugin plugin;

    public EconomyListener(RBalancePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBalanceChange(BalanceChangeEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayerUuid());
        if (player != null && player.isOnline()) {
            plugin.getBossBarSystem().showBalance(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignTrade(SignTradeEvent event) {
        Player player = event.getTrader();
        String type = event.isBuy() ? "Buy" : "Sell";
        String message = String.format("Trading %s x%s → Cost: %s %s", 
            event.getResourceType().name(), 
            event.getAmount().stripTrailingZeros().toPlainString(),
            event.getPrice().stripTrailingZeros().toPlainString(),
            event.getPriceResourceType().getSuffix());
        
        plugin.getBossBarSystem().showTrade(player, message);
        
        Player owner = Bukkit.getPlayer(event.getOwnerUuid());
        if (owner != null && owner.isOnline()) {
             plugin.getBossBarSystem().showBalance(owner);
        }
    }
}
