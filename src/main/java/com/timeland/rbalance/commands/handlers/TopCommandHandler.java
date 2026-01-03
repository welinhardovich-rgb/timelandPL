package com.timeland.rbalance.commands.handlers;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.utils.BalanceFormatter;
import com.timeland.rbalance.utils.ResourceType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class TopCommandHandler {
    private final RBalancePlugin plugin;

    public TopCommandHandler(RBalancePlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /bal top <ресурс>");
            return;
        }

        ResourceType resource = ResourceType.fromString(args[1]);
        if (resource == null) {
            player.sendMessage("§cНеизвестный ресурс.");
            return;
        }

        Map<UUID, Double> allBalances = plugin.getDataManager().getAllBalances(resource);
        
        List<Map.Entry<UUID, Double>> sorted = allBalances.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(10)
                .collect(Collectors.toList());

        player.sendMessage("§6--- Топ 10 по " + resource.name() + " ---");
        int rank = 1;
        for (Map.Entry<UUID, Double> entry : sorted) {
            String name = plugin.getDataManager().getPlayerName(entry.getKey());
            player.sendMessage("§e" + rank + ". §f" + name + ": §a" + BalanceFormatter.format(entry.getValue()));
            rank++;
        }
        player.sendMessage("§6---------------------------");
    }
}
