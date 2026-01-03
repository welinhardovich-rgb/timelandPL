package com.timeland.rbalance.events;

import com.timeland.rbalance.RBalancePlugin;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.UUID;

public class SignProtectionListener implements Listener {
    private final RBalancePlugin plugin;

    public SignProtectionListener(RBalancePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (event.getLine(0).equalsIgnoreCase("[Trade]")) {
            Player player = event.getPlayer();
            // Automatically set owner name on line 1
            event.setLine(1, player.getName());
            plugin.getSignManager().addSign(event.getBlock().getLocation(), player.getUniqueId());
            player.sendMessage("§aТорговая табличка создана!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (Tag.SIGNS.isTagged(block.getType()) || Tag.ALL_HANGING_SIGNS.isTagged(block.getType())) {
            UUID owner = plugin.getSignManager().getOwner(block.getLocation());
            if (owner != null) {
                if (!event.getPlayer().getUniqueId().equals(owner) && !event.getPlayer().hasPermission("rbalance.admin")) {
                    event.getPlayer().sendMessage("§cВы не можете ломать чужую торговую табличку.");
                    event.setCancelled(true);
                } else {
                    plugin.getSignManager().removeSign(block.getLocation());
                    event.getPlayer().sendMessage("§eТорговая табличка удалена.");
                }
            }
        }
    }
}
