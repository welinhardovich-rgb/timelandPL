package com.timeland.rbalance;

import com.timeland.rbalance.commands.BalanceCommand;
import com.timeland.rbalance.events.PlayerDeathListener;
import com.timeland.rbalance.events.PlayerQuitListener;
import com.timeland.rbalance.events.SignInteractListener;
import com.timeland.rbalance.storage.ConfigManager;
import com.timeland.rbalance.storage.DataManager;
import com.timeland.rbalance.systems.*;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class RBalancePlugin extends JavaPlugin {
    private DataManager dataManager;
    private ConfigManager configManager;
    private SignManager signManager;
    private BalanceSystem balanceSystem;
    private CommissionSystem commissionSystem;
    private LimitSystem limitSystem;
    private LogSystem logSystem;
    private TradeSignSystem tradeSignSystem;
    private BossBarSystem bossBarSystem;

    @Override
    public void onEnable() {
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.dataManager = new DataManager(this);
        this.signManager = new SignManager(this);
        
        // Initialize systems
        this.balanceSystem = new BalanceSystem(this);
        this.commissionSystem = new CommissionSystem(this);
        this.limitSystem = new LimitSystem(this);
        this.logSystem = new LogSystem(this);
        this.tradeSignSystem = new TradeSignSystem(this);
        this.bossBarSystem = new BossBarSystem(this);

        // Register commands
        BalanceCommand balanceCommand = new BalanceCommand(this);
        getCommand("bal").setExecutor(balanceCommand);
        getCommand("bal").setTabCompleter(balanceCommand);

        // Register events
        getServer().getPluginManager().registerEvents(new SignInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new EconomyListener(this), this);
        getServer().getPluginManager().registerEvents(new SignProtectionListener(this), this);

        // Periodic save task
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (dataManager != null) dataManager.save();
            if (signManager != null) signManager.save();
        }, 6000L, 6000L); // Every 5 minutes

        getLogger().info("R-Balance plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) dataManager.save();
        if (signManager != null) signManager.save();
        getLogger().info("R-Balance plugin disabled!");
    }
    
    public void reload() {
        configManager.reload();
        dataManager.load();
    }
}
