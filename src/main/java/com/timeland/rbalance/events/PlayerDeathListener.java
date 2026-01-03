package com.timeland.rbalance.events;

import com.timeland.rbalance.RBalancePlugin;
import org.bukkit.event.Listener;

public class PlayerDeathListener implements Listener {
    private final RBalancePlugin plugin;

    public PlayerDeathListener(RBalancePlugin plugin) {
        this.plugin = plugin;
    }
}
