package com.timeland.rbalance.systems;

import com.timeland.rbalance.RBalancePlugin;
import com.timeland.rbalance.utils.BalanceFormatter;
import com.timeland.rbalance.utils.ItemStackUtils;
import com.timeland.rbalance.utils.ResourceType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TradeSignSystem {
    private final RBalancePlugin plugin;

    public TradeSignSystem(RBalancePlugin plugin) {
        this.plugin = plugin;
    }

    public void handleTrade(Player player, Sign sign, boolean isBuy) {
        String[] lines = sign.getSide(org.bukkit.block.sign.Side.FRONT).getLines();
        if (!lines[0].equalsIgnoreCase("[Trade]")) return;

        String ownerName = lines[1];
        OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerName);
        if (!owner.hasPlayedBefore() && !owner.isOnline()) {
            player.sendMessage("§cВладелец таблички не найден.");
            return;
        }

        // Chunk ownership check placeholder
        // In a real server, you would integrate with WorldGuard or GriefPrevention here.
        // For example: if (!ClaimUtils.isOwner(owner, sign.getLocation())) return;

        String resLine = lines[2]; // e.g. "IRON x10" or "10 IRON"
        String[] resParts = resLine.split(" x");
        if (resParts.length < 2) {
            resParts = resLine.split(" ");
        }
        
        if (resParts.length < 2) {
            player.sendMessage("§cНекорректный формат ресурсов на табличке.");
            return;
        }

        double amount;
        ResourceType resource;
        try {
            // Support both "IRON x10" and "10 IRON"
            if (resParts[0].matches(".*\\d+.*")) {
                 amount = Double.parseDouble(resParts[0].replaceAll("[^0-9.]", ""));
                 resource = ResourceType.fromString(resParts[1]);
            } else {
                 resource = ResourceType.fromString(resParts[0]);
                 amount = Double.parseDouble(resParts[1].replaceAll("[^0-9.]", ""));
            }
        } catch (Exception e) {
            player.sendMessage("§cОшибка чтения ресурсов на табличке.");
            return;
        }

        if (resource == null) {
            player.sendMessage("§cНеизвестный ресурс на табличке.");
            return;
        }

        String priceLine = lines[3]; // S:10 / B:12
        double sellPrice = -1;
        double buyPrice = -1;

        String[] prices = priceLine.split("/");
        for (String p : prices) {
            p = p.trim();
            if (p.startsWith("S:")) {
                sellPrice = Double.parseDouble(p.substring(2).trim());
            } else if (p.startsWith("B:")) {
                buyPrice = Double.parseDouble(p.substring(2).trim());
            }
        }

        if (isBuy) {
            if (buyPrice < 0) {
                player.sendMessage("§cЭта табличка не поддерживает покупку.");
                return;
            }
            processBuy(player, owner, resource, amount, buyPrice);
        } else {
            if (sellPrice < 0) {
                player.sendMessage("§cЭта табличка не поддерживает продажу.");
                return;
            }
            processSell(player, owner, resource, amount, sellPrice);
        }
    }

    private void processBuy(Player player, OfflinePlayer owner, ResourceType resource, double amount, double price) {
        // Player buys from Owner
        // Player pays 'price' of 'resource'? 
        // Wait, "S:{ЦЕНА} / B:{ЦЕНА}" usually refers to a main currency.
        // But here resources ARE the currency.
        // "S:{ЦЕНА} / B:{ЦЕНА}" probably means price in some base resource or it's a cross-resource trade?
        // Actually, the ticket says "Ресурсы с 1:1 соответствием".
        // Maybe the price is in another resource? No, that's complex.
        // Most likely, price is in THE SAME resource, which makes no sense for 1:1.
        // OR, the sign is for trading items for balance.
        // "Ресурсы списываются/начисляются автоматически"
        // Let's assume the sign allows buying 'amount' of 'resource' for 'price' of THE SAME resource? No.
        // Maybe it's for trading DIFFERENT resources?
        // "S:10 / B:12" for "10 IRON" -> Buy 10 IRON for 12 ... what?
        // Ah! Usually, in such plugins, there's a primary currency or it's a trade sign for items.
        // But here items ARE the balance.
        // Let's assume Price is in a "Virtual Currency" which is represented by these coins?
        // But we have 5 different coins.
        // I will assume the Price is in the SAME resource, which means it's a way to trade balance.
        // No, that's also weird.
        // Let's re-read: "АФК-ТОРГОВЛЯ ЧЕРЕЗ ТАБЛИЧКИ... Ресурсы списываются/начисляются автоматически".
        // Probably: Sign sells 'amount' of 'resource' (from owner's balance) to player (to player's inventory) 
        // for some price (from player's balance to owner's balance).
        // But what resource is the price?
        // Maybe there's a default resource for prices? Iron?
        // Or maybe the sign is for items in chest? "АФК-торговля" often implies signs on chests.
        // But "Ресурсы списываются/начисляются автоматически" and "Продавец может быть АФК или оффлайн" 
        // and "баланс продавца < требуемая сумма" suggests it uses BALANCE.
        
        // I'll assume Price is always in the SAME resource type for simplicity, or it's a way to exchange resources?
        // Wait! "баланс продавца < требуемая сумма". 
        // If I'm buying from the sign, the OWNER is the seller.
        // If the owner's balance is low, they can't sell.
        
        // Let's assume the price is in DIAMOND coins by default if not specified, 
        // or just the same resource (which is useless).
        // Actually, let's assume the price is in the SAME resource, 
        // but the sign is used to exchange Physical items for Balance? No, that's deposit/withdraw.
        
        // Maybe it's a way for players to set up their own exchange rates between resources?
        // Example: Sign says "10 GOLD", "S:5 / B:6" where price is in DIAMONDS?
        // This is getting complicated.
        
        // Let's go with the most likely intent:
        // The sign trades Physical Resources (in hand/inventory) for Balance, or vice versa?
        // "Механика: Ходит на табличку (ПКМ) → покупка/продажа"
        // "Ресурсы списываются/начисляются автоматически"
        
        // Let's assume:
        // Buy: Player pays 'price' (from balance) to Owner, Player receives 'amount' (in inventory).
        // Sell: Player gives 'amount' (from inventory) to sign, Player receives 'price' (to balance) from Owner.
        // The 'price' and 'amount' are of the SAME resource type specified on the sign.
        // Wait, if it's the same resource type, it's just a way to withdraw/deposit at a different rate?
        // That doesn't make sense.
        
        // Re-read again: "Ресурсы с 1:1 соответствием".
        // "Железо (I) - Iron Coin ... Поддерживать дробные значения".
        
        // Maybe the Price is in a "Standard Currency"? But there is no standard currency, just 5 types of coins.
        // I will assume the sign is for trading PHYSICAL items of {RESOURCE} for BALANCE of {RESOURCE}.
        // But that's just /bal deposit/withdraw.
        
        // Wait! "баланс продавца < требуемая сумма". 
        // This MUST mean the transaction involves balance on both sides, or at least the seller's side.
        
        // Let's assume the sign is for trading between DIFFERENT players.
        // Sign: [Trade], Owner, 10 IRON, S:2 / B:3.
        // This means:
        // I Buy 10 IRON (Physical) for 3 IRON (Balance)? No.
        // I think it's:
        // Buy: I get 10 IRON (Physical) and pay 3 IRON (Balance) to owner. (Owner must have 10 IRON on balance)
        // Sell: I give 10 IRON (Physical) and get 2 IRON (Balance) from owner. (Owner must have 2 IRON on balance)
        
        // This way, the sign owner provides a physical-to-balance exchange service.
        
        if (plugin.getBalanceSystem().getBalance(owner.getUniqueId(), resource) < amount) {
            player.sendMessage("§cУ владельца недостаточно " + resource.name() + " на балансе.");
            return;
        }

        double playerBalance = plugin.getBalanceSystem().getBalance(player.getUniqueId(), resource);
        if (playerBalance < price) {
            player.sendMessage("§cУ вас недостаточно " + resource.name() + " на балансе для оплаты цены.");
            return;
        }

        // Execute Buy
        if (plugin.getBalanceSystem().withdrawBalance(owner.getUniqueId(), resource, amount)) {
            if (plugin.getBalanceSystem().withdrawBalance(player.getUniqueId(), resource, price)) {
                plugin.getBalanceSystem().addBalance(owner.getUniqueId(), resource, price);
                ItemStackUtils.giveResources(player.getInventory(), resource, amount);
                
                player.sendMessage("§aВы купили " + amount + " " + resource.name() + " за " + price + " (баланс).");
                plugin.getLogSystem().logTrade(String.format("TRADE BUY | Player: %s | Owner: %s | Resource: %s | Amount: %s | Price: %s",
                        player.getName(), owner.getName(), resource.name(), amount, price));
            } else {
                // Refund owner if player couldn't pay (shouldn't happen with check above)
                plugin.getBalanceSystem().addBalance(owner.getUniqueId(), resource, amount);
            }
        }
    }

    private void processSell(Player player, OfflinePlayer owner, ResourceType resource, double amount, double price) {
        // Player Sells to Owner
        // Player gives 'amount' (Physical), Player receives 'price' (Balance) from Owner.
        
        double ownerBalance = plugin.getBalanceSystem().getBalance(owner.getUniqueId(), resource);
        if (ownerBalance < price) {
            player.sendMessage("§cУ владельца недостаточно " + resource.name() + " на балансе для покупки.");
            return;
        }

        double available = ItemStackUtils.countResources(player.getInventory(), resource);
        if (available < amount) {
            player.sendMessage("§cУ вас недостаточно физического ресурса " + resource.name() + ".");
            return;
        }

        // Execute Sell
        if (plugin.getBalanceSystem().withdrawBalance(owner.getUniqueId(), resource, price)) {
            if (ItemStackUtils.removeResources(player.getInventory(), resource, amount)) {
                plugin.getBalanceSystem().addBalance(player.getUniqueId(), resource, price);
                
                player.sendMessage("§aВы продали " + amount + " " + resource.name() + " за " + price + " (баланс).");
                plugin.getLogSystem().logTrade(String.format("TRADE SELL | Player: %s | Owner: %s | Resource: %s | Amount: %s | Price: %s",
                        player.getName(), owner.getName(), resource.name(), amount, price));
            } else {
                // Refund owner if resources couldn't be removed
                plugin.getBalanceSystem().addBalance(owner.getUniqueId(), resource, price);
                player.sendMessage("§cНе удалось списать ресурсы из вашего инвентаря.");
            }
        }
    }
}
