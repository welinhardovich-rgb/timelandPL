package com.timeland.rbalance.commands;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.commands.handlers.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BalanceCommand implements CommandExecutor {
    private final RBalancePlugin plugin;
    private final InfoCommandHandler infoHandler;
    private final DepositCommandHandler depositHandler;
    private final WithdrawCommandHandler withdrawHandler;
    private final PayCommandHandler payHandler;
    private final TopCommandHandler topHandler;

    public BalanceCommand(RBalancePlugin plugin) {
        this.plugin = plugin;
        this.infoHandler = new InfoCommandHandler(plugin);
        this.depositHandler = new DepositCommandHandler(plugin);
        this.withdrawHandler = new WithdrawCommandHandler(plugin);
        this.payHandler = new PayCommandHandler(plugin);
        this.topHandler = new TopCommandHandler(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }

        if (args.length == 0) {
            infoHandler.execute(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "deposit":
                depositHandler.execute(player, args);
                break;
            case "withdraw":
                withdrawHandler.execute(player, args);
                break;
            case "pay":
                payHandler.execute(player, args);
                break;
            case "top":
                topHandler.execute(player, args);
                break;
            case "admin":
                if (player.hasPermission("rbalance.admin")) {
                    handleAdmin(player, args);
                } else {
                    player.sendMessage("§cУ вас нет прав.");
                }
                break;
            default:
                player.sendMessage("§cНеизвестная подкоманда.");
                break;
        }

        return true;
    }

    private void handleAdmin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c/bal admin reload");
            return;
        }
        if (args[1].equalsIgnoreCase("reload")) {
            plugin.reload();
            player.sendMessage("§aКонфигурация перезагружена.");
            plugin.getLogSystem().logAdmin("Reloaded configuration by " + player.getName());
        }
    }
}
