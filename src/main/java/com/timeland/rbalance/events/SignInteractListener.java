package com.timeland.rbalance.events;

import com.timeland.rbalance.RBalancePlugin;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class SignInteractListener implements Listener {
    private final RBalancePlugin plugin;

    public SignInteractListener(RBalancePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getDataManager().setPlayerName(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        if (Tag.SIGNS.isTagged(block.getType()) || Tag.ALL_HANGING_SIGNS.isTagged(block.getType())) {
            Sign sign = (Sign) block.getState();
            String firstLine = sign.getSide(org.bukkit.block.sign.Side.FRONT).getLine(0);
            if (firstLine.equalsIgnoreCase("[Trade]")) {
                boolean isBuy = !event.getPlayer().isSneaking();
                plugin.getTradeSignSystem().handleTrade(event.getPlayer(), sign, isBuy);
                event.setCancelled(true);
            }
        }
    }
}
