package com.timeland.rbalance.commands.handlers;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.utils.BalanceFormatter;
import com.timeland.rbalance.utils.ResourceType;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.UUID;

public class InfoCommandHandler {
    private final RBalancePlugin plugin;

    public InfoCommandHandler(RBalancePlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(Player player) {
        player.sendMessage("§6--- Ваш баланс ---");
        UUID uuid = player.getUniqueId();
        for (ResourceType type : ResourceType.values()) {
            BigDecimal bal = plugin.getBalanceSystem().getBalance(uuid, type);
            player.sendMessage("§e" + type.name() + ": §f" + BalanceFormatter.format(bal));
        }
        player.sendMessage("§6------------------");
    }
}
