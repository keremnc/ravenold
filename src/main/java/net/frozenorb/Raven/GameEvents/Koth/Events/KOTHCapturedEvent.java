package net.frozenorb.Raven.GameEvents.Koth.Events;

import lombok.Getter;
import lombok.Setter;
import net.frozenorb.Raven.GameEvents.Koth.KOTH;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
 
public class KOTHCapturedEvent extends PlayerEvent implements Cancellable {
 
    private static final HandlerList handlers = new HandlerList();
 
    @Getter private KOTH KOTH;
    @Getter @Setter private boolean cancelled;
 
    public KOTHCapturedEvent(KOTH KOTH, Player capper) {
        super(capper);
 
        this.KOTH = KOTH;
    }
 
    public HandlerList getHandlers() {
        return (handlers);
    }
 
    public static HandlerList getHandlerList() {
        return (handlers);
    }
 
}