package com.timeland.rbalance.commands.handlers;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.utils.BalanceFormatter;
import com.timeland.rbalance.utils.ItemStackUtils;
import com.timeland.rbalance.utils.ResourceType;
import org.bukkit.entity.Player;

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

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cНекорректное количество.");
            return;
        }

        if (amount <= 0) {
            player.sendMessage("§cКоличество должно быть больше 0.");
            return;
        }

        String limitError = plugin.getLimitSystem().checkWithdraw(player, resource, amount);
        if (limitError != null) {
            player.sendMessage("§c" + limitError);
            return;
        }

        double currentBalance = plugin.getBalanceSystem().getBalance(player.getUniqueId(), resource);
        if (currentBalance < amount) {
            player.sendMessage("§cНедостаточно средств на балансе.");
            return;
        }

        // Apply commission
        double commission = plugin.getCommissionSystem().calculateCommission(amount, "withdraw");
        double toWithdraw = amount + commission; // To get 'amount', you need to pay commission on top? 
        // Ticket: "Применить комиссию... Начислить сожженные ресурсы".
        // Usually, if I want to withdraw 10, and commission is 3%, I might get 9.7 in hand.
        // Let's assume withdrawing 'amount' from balance results in 'amount - commission' in inventory.
        
        double inHand = amount - commission;

        if (plugin.getBalanceSystem().withdrawBalance(player.getUniqueId(), resource, amount)) {
            ItemStackUtils.giveResources(player.getInventory(), resource, inHand);
            plugin.getCommissionSystem().applyBurn(resource, commission);

            player.sendMessage("§aВы сняли " + BalanceFormatter.format(inHand) + " " + resource.name() + ".");
            player.sendMessage("§7Комиссия: " + BalanceFormatter.format(commission) + " (" + plugin.getConfigManager().getCommission("withdraw") + "%)");

            plugin.getLogSystem().logTransaction(String.format("WITHDRAW | Player: %s | Resource: %s | Amount: %s | Commission: %s | Balance: %s",
                    player.getName(), resource.name(), BalanceFormatter.format(amount), BalanceFormatter.format(commission),
                    BalanceFormatter.format(plugin.getBalanceSystem().getBalance(player.getUniqueId(), resource))));
        }
    }
}
