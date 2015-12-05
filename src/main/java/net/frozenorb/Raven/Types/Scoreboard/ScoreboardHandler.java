package net.frozenorb.Raven.Types.Scoreboard;

import net.frozenorb.mCommon.Common;
import org.bukkit.entity.Player;

public class ScoreboardHandler {
    public static boolean isHidden(Player p) {
        return !Common.get().getUserManager().getUser(p).getServerData().getBoolean("hud");
    }
}