package com.timeland.rbalance.events;

import com.timeland.rbalance.RBalancePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final RBalancePlugin plugin;

    public PlayerQuitListener(RBalancePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getBossBarSystem().removePlayer(event.getPlayer());
    }
}
