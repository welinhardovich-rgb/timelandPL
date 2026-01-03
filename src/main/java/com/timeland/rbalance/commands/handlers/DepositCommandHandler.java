package com.timeland.rbalance.commands.handlers;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.utils.BalanceFormatter;
import com.timeland.rbalance.utils.ItemStackUtils;
import com.timeland.rbalance.utils.ResourceType;
import org.bukkit.entity.Player;

public class DepositCommandHandler {
    private final RBalancePlugin plugin;

    public DepositCommandHandler(RBalancePlugin plugin) {
        this.plugin = plugin;
    }

    public void execute(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cИспользование: /bal deposit <ресурс> <кол-во>");
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

        String limitError = plugin.getLimitSystem().checkDeposit(player, resource, amount);
        if (limitError != null) {
            player.sendMessage("§c" + limitError);
            return;
        }

        double available = ItemStackUtils.countResources(player.getInventory(), resource);
        if (available < amount) {
            player.sendMessage("§cУ вас недостаточно ресурсов (доступно: " + BalanceFormatter.format(available) + ").");
            return;
        }

        // Apply commission
        double commission = plugin.getCommissionSystem().calculateCommission(amount, "deposit");
        double toAdd = amount - commission;

        // Process
        if (!ItemStackUtils.removeResources(player.getInventory(), resource, amount)) {
            player.sendMessage("§cНе удалось списать ресурсы. Убедитесь, что у вас есть нужные предметы (например, самородки для дробных значений).");
            return;
        }
        
        plugin.getBalanceSystem().addBalance(player.getUniqueId(), resource, toAdd);
        plugin.getDataManager().addDailyDeposit(player.getUniqueId(), resource, amount);
        plugin.getCommissionSystem().applyBurn(resource, commission);

        player.sendMessage("§aВы внесли " + BalanceFormatter.format(amount) + " " + resource.name() + ".");
        player.sendMessage("§7Комиссия: " + BalanceFormatter.format(commission) + " (" + plugin.getConfigManager().getCommission("deposit") + "%)");
        player.sendMessage("§7Зачислено: " + BalanceFormatter.format(toAdd));

        plugin.getLogSystem().logTransaction(String.format("DEPOSIT | Player: %s | Resource: %s | Amount: %s | Commission: %s | Balance: %s",
                player.getName(), resource.name(), BalanceFormatter.format(amount), BalanceFormatter.format(commission), 
                BalanceFormatter.format(plugin.getBalanceSystem().getBalance(player.getUniqueId(), resource))));
    }
}
