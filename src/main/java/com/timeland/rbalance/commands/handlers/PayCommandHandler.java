package com.timeland.rbalance.commands.handlers;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.utils.BalanceFormatter;
import com.timeland.rbalance.utils.ResourceType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

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

        BigDecimal amount;
        try {
            amount = new BigDecimal(args[3]);
        } catch (Exception e) {
            player.sendMessage("§cНекорректное количество.");
            return;
        }

        if (amount.compareTo(BigDecimal.valueOf(plugin.getConfigManager().getMinOperationAmount())) < 0) {
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

        BigDecimal currentBalance = plugin.getBalanceSystem().getBalance(player.getUniqueId(), resource);
        if (currentBalance.compareTo(amount) < 0) {
            player.sendMessage("§cНедостаточно средств.");
            plugin.getBossBarSystem().showError(player, "INSUFFICIENT BALANCE");
            return;
        }

        BigDecimal commission = plugin.getCommissionSystem().calculateCommission(amount, "transfer");
        BigDecimal toAdd = amount.subtract(commission);

        if (plugin.getBalanceSystem().withdrawBalance(player.getUniqueId(), resource, amount)) {
            plugin.getBalanceSystem().addBalance(target.getUniqueId(), resource, toAdd);
            plugin.getCommissionSystem().applyBurn(resource, commission);

            player.sendMessage("§aВы перевели " + BalanceFormatter.format(amount) + " " + resource.name() + " игроку " + targetName + ".");
            player.sendMessage("§7Комиссия: " + BalanceFormatter.format(commission));
            
            if (target.isOnline()) {
                Player targetPlayer = (Player) target;
                targetPlayer.sendMessage("§aВы получили " + BalanceFormatter.format(toAdd) + " " + resource.name() + " от " + player.getName() + ".");
                plugin.getBossBarSystem().showBalance(targetPlayer);
            }

            plugin.getLogSystem().logTransaction(player, String.format("PAY | To: %s | Resource: %s | Amount: %s | Commission: %s",
                    targetName, resource.name(), BalanceFormatter.format(amount), BalanceFormatter.format(commission)));
        }
    }
}
