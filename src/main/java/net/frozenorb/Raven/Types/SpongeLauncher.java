package net.frozenorb.Raven.Types;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.mCommon.Common;
import net.frozenorb.mCommon.Types.User;

import net.minecraft.server.v1_7_R4.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.mongodb.BasicDBObject;

/**
 * Class to be used to launch players, or to check if they should be launched
 * 
 * @author Kerem
 * @since 10/24/2013
 * 
 */
public class SpongeLauncher {
	private Player player;

	public SpongeLauncher(Player p) {
		player = p;
	}

	/**
	 * Checks if the player is on a sponge, and launches them accordingly
	 */
	public void launch() {
		Location standBlock = player.getWorld().getBlockAt(player.getLocation().add(0.0D, -0.01D, 0.0D)).getLocation();
		if (standBlock.getBlock().getType() == Material.SPONGE) {
			int xblock = 0;
			double xvel = 0.0D;
			int yblock = -1;
			double yvel = 0.0D;
			int zblock = 0;
			double zvel = 0.0D;

			while (standBlock.getBlock().getLocation().add(xblock - 1, -1.0D, 0.0D).getBlock().getType() == Material.SPONGE) {
				xblock--;
				xvel += 1.0D;
			}

			while (standBlock.getBlock().getLocation().add(0.0D, yblock, 0.0D).getBlock().getType() == Material.SPONGE) {
				yblock--;
				yvel += 0.7D;
			}

			while (standBlock.getBlock().getLocation().add(0.0D, -1.0D, zblock - 1).getBlock().getType() == Material.SPONGE) {
				zblock--;
				zvel += 1.0D;
			}

			xblock = 0;
			zblock = 0;
			while (standBlock.getBlock().getLocation().add(xblock + 1, -1.0D, 0.0D).getBlock().getType() == Material.SPONGE) {
				xblock++;
				xvel -= 1.0D;
			}

			while (standBlock.getBlock().getLocation().add(0.0D, -1.0D, zblock + 1).getBlock().getType() == Material.SPONGE) {
				zblock++;
				zvel -= 1.0D;
			}

			if (((xvel != 0.0D) || (yvel != 0.0D) || (zvel != 0.0D))) {
				EntityPlayer pl = ((CraftPlayer) player).getHandle();
				pl.motX = xvel;
				pl.motY = yvel;
				pl.motZ = zvel;
				pl.velocityChanged = true;
				player.playSound(player.getLocation(), Sound.WITHER_HURT, 1.0F, -5.0F);
				User u = Common.get().getUserManager().getUser(player);
				final BasicDBObject o = u.getServerData();
				o.put("noFallDamage", true);
				if (o.containsField("spongeTaskId")) {
					Bukkit.getScheduler().cancelTask(o.getInt("spongeTaskId"));
				}
				o.put("spongeTaskId", Bukkit.getScheduler().runTaskLater(RavenPlugin.get(), new Runnable() {

					@Override
					public void run() {
						o.remove("spongeTaskId");
						o.put("noFallDamage", false);
					}
				}, 200L).getTaskId());
			}
		}

	}
}
