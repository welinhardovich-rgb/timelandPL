package com.timeland.rbalance.systems;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.api.events.SignTradeEvent;
import com.timeland.rbalance.utils.BalanceFormatter;
import com.timeland.rbalance.utils.ResourceType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeSignSystem {
    private final RBalancePlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public TradeSignSystem(RBalancePlugin plugin) {
        this.plugin = plugin;
    }

    public void handleTrade(Player player, Sign sign, boolean isBuy) {
        // Cooldown check
        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(player.getUniqueId(), 0L) > now) {
            player.sendMessage("§cПодождите перед следующей операцией.");
            plugin.getBossBarSystem().showError(player, "TRADE COOLDOWN");
            return;
        }
        cooldowns.put(player.getUniqueId(), now + 1000L); // 1 second cooldown

        String[] lines = sign.getSide(org.bukkit.block.sign.Side.FRONT).getLines();
        if (!lines[0].equalsIgnoreCase("[Trade]")) return;

        String ownerName = lines[1];
        OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerName);
        if (!owner.hasPlayedBefore() && !owner.isOnline()) {
            player.sendMessage("§cВладелец таблички не найден.");
            plugin.getBossBarSystem().showError(player, "OWNER NOT FOUND");
            return;
        }

        // Parse resource and amount
        String resLine = lines[2]; // e.g. "IRON x10"
        String[] resParts = resLine.split(" x");
        if (resParts.length < 2) {
            player.sendMessage("§cНекорректный формат ресурсов: <Resource> x<Amount>");
            return;
        }

        ResourceType resource = ResourceType.fromString(resParts[0]);
        BigDecimal amount;
        try {
            amount = new BigDecimal(resParts[1]);
        } catch (Exception e) {
            player.sendMessage("§cНекорректное количество на табличке.");
            return;
        }

        if (resource == null) {
            player.sendMessage("§cНеизвестный ресурс на табличке.");
            return;
        }

        // Parse prices
        String priceLine = lines[3]; // S:4I / B:5I
        BigDecimal sellPrice = null;
        ResourceType sellResourceType = null;
        BigDecimal buyPrice = null;
        ResourceType buyResourceType = null;

        String[] prices = priceLine.split("/");
        for (String p : prices) {
            p = p.trim();
            if (p.startsWith("S:")) {
                String val = p.substring(2).trim();
                sellResourceType = parseSuffix(val);
                sellPrice = parseValue(val);
            } else if (p.startsWith("B:")) {
                String val = p.substring(2).trim();
                buyResourceType = parseSuffix(val);
                buyPrice = parseValue(val);
            }
        }

        if (isBuy) {
            if (sellPrice == null) {
                player.sendMessage("§cЭта табличка не поддерживает покупку (S:).");
                return;
            }
            processTrade(player, owner, resource, amount, sellPrice, sellResourceType, true);
        } else {
            if (buyPrice == null) {
                player.sendMessage("§cЭта табличка не поддерживает продажу (B:).");
                return;
            }
            processTrade(player, owner, resource, amount, buyPrice, buyResourceType, false);
        }
    }

    private ResourceType parseSuffix(String input) {
        if (input.isEmpty()) return null;
        String suffix = input.substring(input.length() - 1);
        return ResourceType.fromString(suffix);
    }

    private BigDecimal parseValue(String input) {
        if (input.isEmpty()) return null;
        try {
            return new BigDecimal(input.substring(0, input.length() - 1));
        } catch (Exception e) {
            return null;
        }
    }

    private void processTrade(Player trader, OfflinePlayer owner, ResourceType tradeRes, BigDecimal tradeAmount, 
                              BigDecimal price, ResourceType priceRes, boolean isTraderBuying) {
        
        if (priceRes == null) {
            trader.sendMessage("§cНекорREктный формат цены на табличке.");
            return;
        }

        SignTradeEvent event = new SignTradeEvent(trader, owner.getUniqueId(), tradeRes, tradeAmount, price, priceRes, isTraderBuying);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        UUID traderUuid = trader.getUniqueId();
        UUID ownerUuid = owner.getUniqueId();

        if (isTraderBuying) {
            // Trader buys from owner.
            // Trader pays 'price' of 'priceRes' to owner.
            // Owner gives 'tradeAmount' of 'tradeRes' to trader.
            
            if (plugin.getBalanceSystem().getBalance(ownerUuid, tradeRes).compareTo(tradeAmount) < 0) {
                trader.sendMessage("§cУ владельца недостаточно " + tradeRes.name() + " на балансе.");
                plugin.getBossBarSystem().showError(trader, "OWNER OUT OF STOCK");
                return;
            }
            if (plugin.getBalanceSystem().getBalance(traderUuid, priceRes).compareTo(price) < 0) {
                trader.sendMessage("§cУ вас недостаточно " + priceRes.name() + " на балансе.");
                plugin.getBossBarSystem().showError(trader, "INSUFFICIENT BALANCE");
                return;
            }

            // Execute
            if (plugin.getBalanceSystem().withdrawBalance(ownerUuid, tradeRes, tradeAmount)) {
                if (plugin.getBalanceSystem().withdrawBalance(traderUuid, priceRes, price)) {
                    BigDecimal tax = price.multiply(BigDecimal.valueOf(plugin.getConfig().getDouble("commissions.sign_tax", 1.0)))
                            .divide(BigDecimal.valueOf(100.0), 2, java.math.RoundingMode.HALF_UP);
                    BigDecimal toOwner = price.subtract(tax);
                    
                    plugin.getBalanceSystem().addBalance(ownerUuid, priceRes, toOwner);
                    plugin.getBalanceSystem().addBalance(traderUuid, tradeRes, tradeAmount);
                    plugin.getCommissionSystem().applyBurn(priceRes, tax);
                    
                    trader.sendMessage("§aВы купили " + tradeAmount + " " + tradeRes.name() + " за " + price + " " + priceRes.name() + ".");
                    if (tax.compareTo(BigDecimal.ZERO) > 0) trader.sendMessage("§7(Налог: " + tax + " " + priceRes.getSuffix() + ")");
                } else {
                    plugin.getBalanceSystem().addBalance(ownerUuid, tradeRes, tradeAmount); // refund
                }
            }
        } else {
            // Trader sells to owner.
            // Trader gives 'tradeAmount' of 'tradeRes' to owner.
            // Owner pays 'price' of 'priceRes' to trader.
            
            if (plugin.getBalanceSystem().getBalance(traderUuid, tradeRes).compareTo(tradeAmount) < 0) {
                trader.sendMessage("§cУ вас недостаточно " + tradeRes.name() + " на балансе.");
                plugin.getBossBarSystem().showError(trader, "INSUFFICIENT BALANCE");
                return;
            }
            if (plugin.getBalanceSystem().getBalance(ownerUuid, priceRes).compareTo(price) < 0) {
                trader.sendMessage("§cУ владельца недостаточно " + priceRes.name() + " для покупки.");
                plugin.getBossBarSystem().showError(trader, "OWNER OUT OF BALANCE");
                return;
            }

            // Execute
            if (plugin.getBalanceSystem().withdrawBalance(traderUuid, tradeRes, tradeAmount)) {
                if (plugin.getBalanceSystem().withdrawBalance(ownerUuid, priceRes, price)) {
                    BigDecimal tax = price.multiply(BigDecimal.valueOf(plugin.getConfig().getDouble("commissions.sign_tax", 1.0)))
                            .divide(BigDecimal.valueOf(100.0), 2, java.math.RoundingMode.HALF_UP);
                    BigDecimal toTrader = price.subtract(tax);

                    plugin.getBalanceSystem().addBalance(traderUuid, priceRes, toTrader);
                    plugin.getBalanceSystem().addBalance(ownerUuid, tradeRes, tradeAmount);
                    plugin.getCommissionSystem().applyBurn(priceRes, tax);
                    
                    trader.sendMessage("§aВы продали " + tradeAmount + " " + tradeRes.name() + " за " + price + " " + priceRes.name() + ".");
                    if (tax.compareTo(BigDecimal.ZERO) > 0) trader.sendMessage("§7(Налог: " + tax + " " + priceRes.getSuffix() + ")");
                } else {
                    plugin.getBalanceSystem().addBalance(traderUuid, tradeRes, tradeAmount); // refund
                }
            }
        }
        updateSignState(sign);
    }

    public void updateSignState(Sign sign) {
        String[] lines = sign.getSide(org.bukkit.block.sign.Side.FRONT).getLines();
        String ownerName = lines[1];
        OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerName);
        
        String resLine = lines[2];
        String[] resParts = resLine.split(" x");
        if (resParts.length < 2) return;
        ResourceType resource = ResourceType.fromString(resParts[0]);
        BigDecimal amount;
        try { amount = new BigDecimal(resParts[1]); } catch (Exception e) { return; }
        if (resource == null) return;

        BigDecimal ownerTradeBal = plugin.getBalanceSystem().getBalance(owner.getUniqueId(), resource);
        
        String state;
        if (ownerTradeBal.compareTo(amount) < 0) {
            state = "§c[OUT OF STOCK]";
        } else {
            state = "§a[Trade]";
        }
        
        sign.getSide(org.bukkit.block.sign.Side.FRONT).setLine(0, state);
        sign.update();
    }
}
