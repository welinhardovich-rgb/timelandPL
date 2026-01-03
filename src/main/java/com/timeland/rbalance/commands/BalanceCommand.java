package com.timeland.rbalance.commands;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.commands.handlers.*;
import com.timeland.rbalance.utils.ResourceType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BalanceCommand implements CommandExecutor, TabCompleter {
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
            case "bossbar":
                handleBossBar(player, args);
                break;
            case "history":
                player.sendMessage("§6--- Последние 10 транзакций ---");
                java.util.List<String> userHistory = plugin.getDataManager().getHistory(player.getUniqueId());
                if (userHistory.isEmpty()) {
                    player.sendMessage("§7Нет записей.");
                } else {
                    for (String entry : userHistory) {
                        player.sendMessage("§7- " + entry);
                    }
                }
                player.sendMessage("§6-----------------------------");
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

    private void handleBossBar(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /bal bossbar <on|off>");
            return;
        }
        boolean state = args[1].equalsIgnoreCase("on");
        plugin.getBossBarSystem().setEnabled(player.getUniqueId(), state);
        player.sendMessage("§aBossBar " + (state ? "включен" : "выключен") + ".");
    }

    private void handleAdmin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c/bal admin reload");
            player.sendMessage("§c/bal admin set <player> <resource> <amount>");
            player.sendMessage("§c/bal admin reset <player>");
            return;
        }
        String sub = args[1].toLowerCase();
        if (sub.equalsIgnoreCase("reload")) {
            plugin.reload();
            player.sendMessage("§aКонфигурация перезагружена.");
            plugin.getLogSystem().logAdmin("Reloaded configuration by " + player.getName());
        } else if (sub.equalsIgnoreCase("set")) {
            if (args.length < 5) {
                player.sendMessage("§c/bal admin set <player> <resource> <amount>");
                return;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
            ResourceType res = ResourceType.fromString(args[3]);
            BigDecimal amount;
            try {
                amount = new BigDecimal(args[4]);
            } catch (Exception e) {
                player.sendMessage("§cОшибка: некорректная сумма.");
                return;
            }
            if (res == null) {
                player.sendMessage("§cОшибка: неизвестный ресурс.");
                return;
            }
            plugin.getBalanceSystem().setBalance(target.getUniqueId(), res, amount);
            player.sendMessage("§aБаланс " + target.getName() + " по " + res.name() + " установлен в " + amount);
            plugin.getLogSystem().logAdmin(String.format("Set balance of %s (%s) to %s by %s", target.getName(), res.name(), amount, player.getName()));
        } else if (sub.equalsIgnoreCase("reset")) {
            if (args.length < 3) {
                player.sendMessage("§c/bal admin reset <player>");
                return;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
            for (ResourceType res : ResourceType.values()) {
                plugin.getBalanceSystem().setBalance(target.getUniqueId(), res, BigDecimal.ZERO);
            }
            player.sendMessage("§aБаланс " + target.getName() + " полностью сброшен.");
            plugin.getLogSystem().logAdmin("Reset all balances of " + target.getName() + " by " + player.getName());
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>(Arrays.asList("deposit", "withdraw", "pay", "top", "bossbar", "history"));
            if (sender.hasPermission("rbalance.admin")) {
                subCommands.add("admin");
            }
            return filter(subCommands, args[0]);
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "deposit":
                case "withdraw":
                case "top":
                    return filter(getResourceNames(), args[1]);
                case "pay":
                    return null; // Player names
                case "bossbar":
                    return filter(Arrays.asList("on", "off"), args[1]);
                case "admin":
                    if (sender.hasPermission("rbalance.admin")) {
                        return filter(Arrays.asList("reload", "set", "reset"), args[1]);
                    }
                    break;
            }
        }

        if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "pay":
                    return filter(getResourceNames(), args[2]);
                case "admin":
                    if (sender.hasPermission("rbalance.admin")) {
                        String sub = args[1].toLowerCase();
                        if (sub.equals("set") || sub.equals("reset")) {
                            return null; // Player names
                        }
                    }
                    break;
            }
        }

        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("admin") && sender.hasPermission("rbalance.admin")) {
                if (args[1].equalsIgnoreCase("set")) {
                    return filter(getResourceNames(), args[3]);
                }
            }
        }

        return Collections.emptyList();
    }

    private List<String> getResourceNames() {
        return Arrays.stream(ResourceType.values())
                .map(Enum::name)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    private List<String> filter(List<String> list, String input) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}
