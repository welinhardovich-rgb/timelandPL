package com.timeland.rbalance.utils;

import org.bukkit.Material;
import java.util.HashMap;
import java.util.Map;

public enum ResourceType {
    IRON(Material.IRON_INGOT, Material.IRON_NUGGET, Material.IRON_BLOCK),
    GOLD(Material.GOLD_INGOT, Material.GOLD_NUGGET, Material.GOLD_BLOCK),
    DIAMOND(Material.DIAMOND, null, Material.DIAMOND_BLOCK),
    NETHERITE(Material.NETHERITE_INGOT, null, Material.NETHERITE_BLOCK),
    EMERALD(Material.EMERALD, null, Material.EMERALD_BLOCK);

    private final Material ingot;
    private final Material nugget;
    private final Material block;

    ResourceType(Material ingot, Material nugget, Material block) {
        this.ingot = ingot;
        this.nugget = nugget;
        this.block = block;
    }

    public Material getIngot() {
        return ingot;
    }

    public Material getNugget() {
        return nugget;
    }

    public Material getBlock() {
        return block;
    }

    public static ResourceType fromString(String name) {
        try {
            return ResourceType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Map<Material, Double> getValues() {
        Map<Material, Double> values = new HashMap<>();
        if (ingot != null) values.put(ingot, 1.0);
        if (nugget != null) values.put(nugget, 0.1);
        if (block != null) values.put(block, 9.0);
        return values;
    }
}
