package net.frozenorb.Raven.GameEvents.Koth.Events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.frozenorb.Raven.GameEvents.Koth.KOTH;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
 
@AllArgsConstructor
public class KOTHControlTickEvent extends Event {
 
    private static final HandlerList handlers = new HandlerList();
 
    @Getter
    private KOTH KOTH;
 
    public HandlerList getHandlers() {
        return (handlers);
    }
 
    public static HandlerList getHandlerList() {
        return (handlers);
    }
 
}