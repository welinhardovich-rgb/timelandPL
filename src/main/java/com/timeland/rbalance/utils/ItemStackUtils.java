package com.timeland.rbalance.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class ItemStackUtils {

    public static BigDecimal countResources(Inventory inventory, ResourceType type) {
        BigDecimal total = BigDecimal.ZERO;
        Map<Material, BigDecimal> values = type.getValues();
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType().isAir()) continue;
            if (values.containsKey(item.getType())) {
                total = total.add(BigDecimal.valueOf(item.getAmount()).multiply(values.get(item.getType())));
            }
        }
        return total;
    }

    public static boolean removeResources(Inventory inventory, ResourceType type, BigDecimal amount) {
        BigDecimal available = countResources(inventory, type);
        if (available.compareTo(amount) < 0) return false;

        BigDecimal remaining = amount;
        Map<Material, BigDecimal> values = type.getValues();
        Material[] order = {type.getBlock(), type.getIngot(), type.getNugget()};
        
        for (Material material : order) {
            if (material == null) continue;
            BigDecimal value = values.get(material);
            for (int i = 0; i < inventory.getSize(); i++) {
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
                ItemStack item = inventory.getItem(i);
                if (item == null || item.getType() != material) continue;
                
                int itemAmount = item.getAmount();
                int toRemove = remaining.divide(value, 0, RoundingMode.FLOOR).intValue();
                toRemove = Math.min(itemAmount, toRemove);
                
                if (toRemove > 0) {
                    if (toRemove == itemAmount) {
                        inventory.setItem(i, null);
                    } else {
                        item.setAmount(itemAmount - toRemove);
                    }
                    remaining = remaining.subtract(BigDecimal.valueOf(toRemove).multiply(value));
                }
            }
        }
        
        return remaining.compareTo(BigDecimal.ZERO) <= 0;
    }

    public static void giveResources(Inventory inventory, ResourceType type, BigDecimal amount) {
        BigDecimal remaining = amount;

        // Give blocks
        if (type.getBlock() != null) {
            int blocks = remaining.divide(BigDecimal.valueOf(9.0), 0, RoundingMode.FLOOR).intValue();
            if (blocks > 0) {
                inventory.addItem(new ItemStack(type.getBlock(), blocks));
                remaining = remaining.subtract(BigDecimal.valueOf(blocks).multiply(BigDecimal.valueOf(9.0)));
            }
        }

        // Give ingots
        if (type.getIngot() != null) {
            int ingots = remaining.divide(BigDecimal.valueOf(1.0), 0, RoundingMode.FLOOR).intValue();
            if (ingots > 0) {
                inventory.addItem(new ItemStack(type.getIngot(), ingots));
                remaining = remaining.subtract(BigDecimal.valueOf(ingots).multiply(BigDecimal.valueOf(1.0)));
            }
        }

        // Give nuggets
        if (type.getNugget() != null) {
            int nuggets = remaining.divide(BigDecimal.valueOf(0.1), 0, RoundingMode.HALF_UP).intValue();
            if (nuggets > 0) {
                inventory.addItem(new ItemStack(type.getNugget(), nuggets));
            }
        }
    }
}
