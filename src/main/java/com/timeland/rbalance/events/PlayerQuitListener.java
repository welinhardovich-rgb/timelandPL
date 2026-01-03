package com.timeland.rbalance.events;

import com.timeland.rbalance.RBalancePlugin;
import org.bukkit.event.Listener;

public class PlayerQuitListener implements Listener {
    private final RBalancePlugin plugin;

    public PlayerQuitListener(RBalancePlugin plugin) {
        this.plugin = plugin;
    }
}
