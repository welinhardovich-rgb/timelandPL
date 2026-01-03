package com.timeland.rbalance.api.events;

import com.timeland.rbalance.utils.ResourceType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class SignTradeEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player trader;
    private final UUID ownerUuid;
    private final ResourceType resourceType;
    private final BigDecimal amount;
    private final BigDecimal price;
    private final ResourceType priceResourceType;
    private final boolean isBuy; // true if trader buys from owner
    @Setter
    private boolean cancelled;

    public SignTradeEvent(Player trader, UUID ownerUuid, ResourceType resourceType, BigDecimal amount, BigDecimal price, ResourceType priceResourceType, boolean isBuy) {
        this.trader = trader;
        this.ownerUuid = ownerUuid;
        this.resourceType = resourceType;
        this.amount = amount;
        this.price = price;
        this.priceResourceType = priceResourceType;
        this.isBuy = isBuy;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
