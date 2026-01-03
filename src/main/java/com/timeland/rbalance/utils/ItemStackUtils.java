package com.timeland.rbalance.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ItemStackUtils {

    public static double countResources(Inventory inventory, ResourceType type) {
        double total = 0;
        Map<Material, Double> values = type.getValues();
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType().isAir()) continue;
            if (values.containsKey(item.getType())) {
                total += item.getAmount() * values.get(item.getType());
            }
        }
        return total;
    }

    public static boolean removeResources(Inventory inventory, ResourceType type, double amount) {
        double available = countResources(inventory, type);
        if (available < amount - 0.0001) return false;

        double remaining = amount;
        Map<Material, Double> values = type.getValues();
        Material[] order = {type.getBlock(), type.getIngot(), type.getNugget()};
        
        // First pass: try to remove as much as possible using whole items
        for (Material material : order) {
            if (material == null) continue;
            double value = values.get(material);
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null || item.getType() != material) continue;
                
                int itemAmount = item.getAmount();
                int toRemove = (int) Math.min(itemAmount, Math.floor(remaining / value + 0.0001));
                
                if (toRemove > 0) {
                    if (toRemove == itemAmount) {
                        inventory.setItem(i, null);
                    } else {
                        item.setAmount(itemAmount - toRemove);
                    }
                    remaining -= toRemove * value;
                }
            }
        }
        
        return remaining < 0.0001;
    }

    public static void giveResources(Inventory inventory, ResourceType type, double amount) {
        double remaining = amount;

        // Give blocks
        if (type.getBlock() != null) {
            int blocks = (int) Math.floor(remaining / 9.0 + 0.0001);
            if (blocks > 0) {
                inventory.addItem(new ItemStack(type.getBlock(), blocks));
                remaining -= blocks * 9.0;
            }
        }

        // Give ingots
        if (type.getIngot() != null) {
            int ingots = (int) Math.floor(remaining / 1.0 + 0.0001);
            if (ingots > 0) {
                inventory.addItem(new ItemStack(type.getIngot(), ingots));
                remaining -= ingots * 1.0;
            }
        }

        // Give nuggets
        if (type.getNugget() != null) {
            int nuggets = (int) Math.round(remaining / 0.1);
            if (nuggets > 0) {
                inventory.addItem(new ItemStack(type.getNugget(), nuggets));
            }
        } else if (remaining > 0.0001 && type.getIngot() != null) {
            // If no nuggets, round up to 1 ingot? No, that's unfair.
            // Just leave the remainder if it's too small and no nugget exists.
        }
    }
}
