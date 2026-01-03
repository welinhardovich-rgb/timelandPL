package com.timeland.rbalance.systems;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.api.events.BossBarUpdateEvent;
import com.timeland.rbalance.utils.ResourceType;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarSystem {
    private final RBalancePlugin plugin;
    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private final Map<UUID, Integer> hideTasks = new HashMap<>();

    public BossBarSystem(RBalancePlugin plugin) {
        this.plugin = plugin;
    }

    public void setEnabled(UUID uuid, boolean state) {
        plugin.getDataManager().setBossBarEnabled(uuid, state);
        if (!state) {
            hideBossBar(uuid);
        }
    }

    public boolean isEnabled(UUID uuid) {
        return plugin.getDataManager().isBossBarEnabled(uuid);
    }

    public void showBalance(Player player) {
        if (!isEnabled(player.getUniqueId())) return;

        StringBuilder sb = new StringBuilder();
        for (ResourceType type : ResourceType.values()) {
            BigDecimal bal = plugin.getBalanceSystem().getBalance(player.getUniqueId(), type);
            sb.append(type.getSuffix()).append(": ").append(bal.stripTrailingZeros().toPlainString());
            if (type != ResourceType.NETHERITE) sb.append(" | ");
        }

        updateBossBar(player, sb.toString(), BarColor.GREEN);
    }

    public void showTrade(Player player, String message) {
        if (!isEnabled(player.getUniqueId())) return;
        updateBossBar(player, message, BarColor.YELLOW);
    }

    public void showError(Player player, String message) {
        if (!isEnabled(player.getUniqueId())) return;
        updateBossBar(player, message, BarColor.RED);
    }

    private void updateBossBar(Player player, String content, BarColor color) {
        BossBarUpdateEvent event = new BossBarUpdateEvent(player, content);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        UUID uuid = player.getUniqueId();
        BossBar bar = bossBars.computeIfAbsent(uuid, k -> 
            Bukkit.createBossBar("", color, BarStyle.SOLID));
        
        bar.setTitle(event.getContent());
        bar.setColor(color);
        bar.setProgress(1.0);
        bar.addPlayer(player);
        bar.setVisible(true);

        // Schedule hide
        cancelHideTask(uuid);
        long delay = plugin.getConfig().getLong("bossbar.hide_delay", 100L);
        int taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            bar.setVisible(false);
            hideTasks.remove(uuid);
        }, delay).getTaskId();
        hideTasks.put(uuid, taskId);
    }

    private void hideBossBar(UUID uuid) {
        BossBar bar = bossBars.get(uuid);
        if (bar != null) {
            bar.setVisible(false);
        }
        cancelHideTask(uuid);
    }

    private void cancelHideTask(UUID uuid) {
        if (hideTasks.containsKey(uuid)) {
            Bukkit.getScheduler().cancelTask(hideTasks.get(uuid));
            hideTasks.remove(uuid);
        }
    }
    
    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        BossBar bar = bossBars.remove(uuid);
        if (bar != null) {
            bar.removeAll();
        }
        cancelHideTask(uuid);
    }
}
