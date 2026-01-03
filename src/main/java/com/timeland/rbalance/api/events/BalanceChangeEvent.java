package com.timeland.rbalance.api.events;

import com.timeland.rbalance.utils.ResourceType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class BalanceChangeEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final UUID playerUuid;
    private final ResourceType resourceType;
    private final BigDecimal oldBalance;
    @Setter
    private BigDecimal newBalance;
    @Setter
    private boolean cancelled;

    public BalanceChangeEvent(UUID playerUuid, ResourceType resourceType, BigDecimal oldBalance, BigDecimal newBalance) {
        this.playerUuid = playerUuid;
        this.resourceType = resourceType;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
