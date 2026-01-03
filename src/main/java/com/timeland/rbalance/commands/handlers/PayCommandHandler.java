package com.timeland.rbalance.commands.handlers;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.utils.BalanceFormatter;
import com.timeland.rbalance.utils.ResourceType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PayCommandHandler {
    private final RBalancePlugin plugin;

    public PayCommandHandler(RBalancePlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§cИспользование: /bal pay <ник> <ресурс> <кол-во>");
            return;
        }

        String targetName = args[1];
        ResourceType resource = ResourceType.fromString(args[2]);
        if (resource == null) {
            player.sendMessage("§cНеизвестный ресурс.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cНекорректное количество.");
            return;
        }

        if (amount <= plugin.getConfigManager().getMinOperationAmount()) {
            player.sendMessage("§cМинимальная сумма: " + plugin.getConfigManager().getMinOperationAmount());
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage("§cИгрок не найден.");
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§cВы не можете перевести самому себе.");
            return;
        }

        double currentBalance = plugin.getBalanceSystem().getBalance(player.getUniqueId(), resource);
        if (currentBalance < amount) {
            player.sendMessage("§cНедостаточно средств.");
            return;
        }

        double commission = plugin.getCommissionSystem().calculateCommission(amount, "transfer");
        double toAdd = amount - commission;

        if (plugin.getBalanceSystem().withdrawBalance(player.getUniqueId(), resource, amount)) {
            plugin.getBalanceSystem().addBalance(target.getUniqueId(), resource, toAdd);
            plugin.getCommissionSystem().applyBurn(resource, commission);

            player.sendMessage("§aВы перевели " + BalanceFormatter.format(amount) + " " + resource.name() + " игроку " + targetName + ".");
            player.sendMessage("§7Комиссия: " + BalanceFormatter.format(commission));
            
            if (target.isOnline()) {
                ((Player) target).sendMessage("§aВы получили " + BalanceFormatter.format(toAdd) + " " + resource.name() + " от " + player.getName() + ".");
            }

            plugin.getLogSystem().logTransaction(String.format("PAY | From: %s | To: %s | Resource: %s | Amount: %s | Commission: %s",
                    player.getName(), targetName, resource.name(), BalanceFormatter.format(amount), BalanceFormatter.format(commission)));
        }
    }
}
