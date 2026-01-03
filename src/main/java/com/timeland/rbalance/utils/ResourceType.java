package com.timeland.rbalance.utils;

import org.bukkit.Material;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public enum ResourceType {
    IRON(Material.IRON_INGOT, Material.IRON_NUGGET, Material.IRON_BLOCK, "I"),
    GOLD(Material.GOLD_INGOT, Material.GOLD_NUGGET, Material.GOLD_BLOCK, "G"),
    DIAMOND(Material.DIAMOND, null, Material.DIAMOND_BLOCK, "D"),
    EMERALD(Material.EMERALD, null, Material.EMERALD_BLOCK, "E"),
    NETHERITE(Material.NETHERITE_INGOT, null, Material.NETHERITE_BLOCK, "N");

    private final Material ingot;
    private final Material nugget;
    private final Material block;
    private final String suffix;

    ResourceType(Material ingot, Material nugget, Material block, String suffix) {
        this.ingot = ingot;
        this.nugget = nugget;
        this.block = block;
        this.suffix = suffix;
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

    public String getSuffix() {
        return suffix;
    }

    public static ResourceType fromString(String name) {
        if (name == null) return null;
        for (ResourceType type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.suffix.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public Map<Material, BigDecimal> getValues() {
        Map<Material, BigDecimal> values = new HashMap<>();
        if (ingot != null) values.put(ingot, BigDecimal.valueOf(1.0));
        if (nugget != null) values.put(nugget, BigDecimal.valueOf(0.1));
        if (block != null) values.put(block, BigDecimal.valueOf(9.0));
        return values;
    }
}
