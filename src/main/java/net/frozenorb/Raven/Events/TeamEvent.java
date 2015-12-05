package net.frozenorb.Raven.Events;

import lombok.NonNull;
import net.frozenorb.Raven.Team.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;

/**
 * Created by Ryan on 3/24/2015
 * <p/>
 * Project: raven
 */
public class TeamEvent extends Event {

    static HandlerList list = new HandlerList();

    @Nullable
    Team team;
    @NonNull
    Player teamPlayer;

    public TeamEvent(Player teamPlayer, @Nullable Team team) {
        this.team = team;
        this.teamPlayer = teamPlayer;
    }

    public Player getTeamPlayer() {
        return teamPlayer;
    }

    @Nullable
    public Team getTeam() {
        return team;
    }

    public static HandlerList getHandlerList() {
        return list;
    }

    @Override
    public HandlerList getHandlers() {
        return list;
    }
}
