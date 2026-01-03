package com.timeland.rbalance.commands.handlers;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.utils.BalanceFormatter;
import com.timeland.rbalance.utils.ItemStackUtils;
import com.timeland.rbalance.utils.ResourceType;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class WithdrawCommandHandler {
    private final RBalancePlugin plugin;

    public WithdrawCommandHandler(RBalancePlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cИспользование: /bal withdraw <ресурс> <кол-во>");
            return;
        }

        ResourceType resource = ResourceType.fromString(args[1]);
        if (resource == null) {
            player.sendMessage("§cНеизвестный ресурс.");
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(args[2]);
        } catch (Exception e) {
            player.sendMessage("§cНекорректное количество.");
            return;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            player.sendMessage("§cКоличество должно быть больше 0.");
            return;
        }

        String limitError = plugin.getLimitSystem().checkWithdraw(player, resource, amount);
        if (limitError != null) {
            player.sendMessage("§c" + limitError);
            plugin.getBossBarSystem().showError(player, limitError);
            return;
        }

        BigDecimal currentBalance = plugin.getBalanceSystem().getBalance(player.getUniqueId(), resource);
        if (currentBalance.compareTo(amount) < 0) {
            player.sendMessage("§cНедостаточно средств на балансе.");
            plugin.getBossBarSystem().showError(player, "INSUFFICIENT BALANCE");
            return;
        }

        // Apply commission
        BigDecimal commission = plugin.getCommissionSystem().calculateCommission(amount, "withdraw");
        BigDecimal inHand = amount.subtract(commission);

        if (plugin.getBalanceSystem().withdrawBalance(player.getUniqueId(), resource, amount)) {
            ItemStackUtils.giveResources(player.getInventory(), resource, inHand);
            plugin.getCommissionSystem().applyBurn(resource, commission);

            player.sendMessage("§aВы сняли " + BalanceFormatter.format(inHand) + " " + resource.name() + ".");
            player.sendMessage("§7Комиссия: " + BalanceFormatter.format(commission) + " (" + plugin.getConfigManager().getCommission("withdraw") + "%)");

            plugin.getLogSystem().logTransaction(player, String.format("WITHDRAW | Resource: %s | Amount: %s | Commission: %s | Balance: %s",
                    resource.name(), BalanceFormatter.format(amount), BalanceFormatter.format(commission),
                    BalanceFormatter.format(plugin.getBalanceSystem().getBalance(player.getUniqueId(), resource))));
        }
    }
}
