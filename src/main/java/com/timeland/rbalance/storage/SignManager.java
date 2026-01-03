package com.timeland.rbalance.storage;

import com.timeland.rbalance.RBalancePlugin;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SignManager {
    private final YamlHandler signsYaml;
    private final Map<Location, UUID> signOwners = new HashMap<>();

    public SignManager(RBalancePlugin plugin) {
        this.signsYaml = new YamlHandler(plugin, "signs.yml");
        load();
    }

    public void load() {
        signsYaml.reload();
        signOwners.clear();
        ConfigurationSection section = signsYaml.getConfig().getConfigurationSection("signs");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                UUID owner = UUID.fromString(section.getString(key));
                Location loc = stringToLocation(key);
                if (loc != null) {
                    signOwners.put(loc, owner);
                }
            }
        }
    }

    public void save() {
        signsYaml.getConfig().set("signs", null);
        for (Map.Entry<Location, UUID> entry : signOwners.entrySet()) {
            signsYaml.getConfig().set("signs." + locationToString(entry.getKey()), entry.getValue().toString());
        }
        signsYaml.save();
    }

    public void addSign(Location loc, UUID owner) {
        signOwners.put(loc, owner);
    }

    public void removeSign(Location loc) {
        signOwners.remove(loc);
    }

    public UUID getOwner(Location loc) {
        return signOwners.get(loc);
    }

    private String locationToString(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private Location stringToLocation(String s) {
        String[] parts = s.split(",");
        if (parts.length < 4) return null;
        return new Location(org.bukkit.Bukkit.getWorld(parts[0]), 
            Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
    }
}
