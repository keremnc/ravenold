package net.frozenorb.Raven.WorldControl;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Types.Cuboid;
import net.frozenorb.Utilities.Core;
import net.frozenorb.Utilities.Interfaces.Callback;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("deprecation")
public class SpawnResetter {
	private static final int MAX_BLOCK_CHANGES_PER_PASS = 200;

	private static final int SPAWN_MAX_X;
	private static final int SPAWN_MAX_Z;
	private static final int SPAWN_MIN_X;
	private static final int SPAWN_MIN_Z;

	private static final int SPAWN_Y_LEVEL;

	public static final int STONE_BORDER;

	private int blocksToChange = 0;
	private ArrayList<Block> queued = new ArrayList<Block>();
	private BukkitRunnable runnable;
	private AtomicInteger blocksChanged = new AtomicInteger();
	private ArrayList<Long> averageMillisPerPass = new ArrayList<Long>();
	private long taskStarted;
	private int passes = 0;

	private static Cuboid mainArea;

	static {
		FileConfiguration config = RavenPlugin.get().getConfig();
		SPAWN_MAX_X = config.getInt("SPAWN_MAX_X");
		SPAWN_MAX_Z = config.getInt("SPAWN_MAX_Z");
		SPAWN_MIN_X = config.getInt("SPAWN_MIN_X");
		SPAWN_MIN_Z = config.getInt("SPAWN_MIN_Z");
		SPAWN_Y_LEVEL = config.getInt("SPAWN_Y_LEVEL");
		STONE_BORDER = config.getInt("STONE_BORDER");
		World w = Bukkit.getWorlds().get(0);
		mainArea = new Cuboid(w, SPAWN_MAX_X, 256, SPAWN_MAX_Z, SPAWN_MIN_X, 0, SPAWN_MIN_Z);

	}

	public static boolean isValid() {
		return !(STONE_BORDER == 0);
	}

	/**
	 * Starts the repeating task that iterates and clears queued blocks
	 */
	public void clear(final Callback<String> cb) {
		taskStarted = System.currentTimeMillis();
		blocksChanged.set(0);
		passes = 0;
		try {
			if (runnable == null) {
				runnable = new BukkitRunnable() {
					public void run() {
						passes++;
						final long n = System.currentTimeMillis();
						if (queued.size() == 0) {
							runnable = null;
							long timeTaken = System.currentTimeMillis() - taskStarted;
							String msg = "§eThe spawn clearing task has finished.\n§eChanged " + blocksChanged.get() + " blocks in " + Core.get().getConvertedTime(timeTaken / 1000).trim() + ".";
							cb.callback(msg);
							cancel();
						}
						int i = 0;
						ArrayList<Block> toDo = new ArrayList<Block>();
						for (Block b : queued) {
							if (i++ >= MAX_BLOCK_CHANGES_PER_PASS) {
								break;
							}
							toDo.add(b);
						}
						for (Block b : toDo) {
							checkLoadedChunk(b.getLocation());
							queued.remove(b);
							if (b.getY() == SPAWN_Y_LEVEL) {
								if (b.setTypeId(Material.STONE.getId(), false)) {
									blocksChanged.incrementAndGet();

								}
							} else {
								if (b.setTypeId(0, false)) {
									blocksChanged.incrementAndGet();
								}
							}
						}
						averageMillisPerPass.add(System.currentTimeMillis() - n);
					}
				};
				runnable.runTaskTimer(RavenPlugin.get(), 2, 3);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Checks if the given location is loaded or not
	 * 
	 * @param pt
	 *            the location to load
	 */
	public void checkLoadedChunk(Location pt) {
		if (!pt.getWorld().isChunkLoaded(pt.getBlockX() >> 4, pt.getBlockZ() >> 4))
			pt.getWorld().loadChunk(pt.getBlockX() >> 4, pt.getBlockZ() >> 4);
	}

	/**
	 * Cancels the spawn reset task, if one exists
	 * 
	 * @return if the cancellation was successfully done
	 */
	public boolean cancel() {
		if (runnable != null) {
			runnable.cancel();
			return true;
		}
		return false;
	}

	/**
	 * Fills the list of blocks to clear with blocks, and times the calculations
	 * .
	 * 
	 * @return amount of blocks to change
	 */
	public int populate() {
		int blocks = 0;
		int xLimit = (STONE_BORDER * 2) + 1;
		int zLimit = (STONE_BORDER * 2) + 1;
		World w = Bukkit.getWorlds().get(0);
		int x, z, dx, dz;
		x = z = dx = 0;
		dz = -1;
		int t = Math.max(xLimit, zLimit);
		int maxI = t * t;
		for (int i = 0; i < maxI; i++) {
			if ((-xLimit / 2 <= x) && (x <= xLimit / 2) && (-zLimit / 2 <= z) && (z <= zLimit / 2)) {
				if (!mainArea.contains(x, z)) {
					for (int y = SPAWN_Y_LEVEL; y < 256; y++) {
						Block b = w.getBlockAt(new Location(w, x, y, z));
						if ((b.getY() != SPAWN_Y_LEVEL && b.getType() != Material.AIR) || (!b.getType().equals(Material.STONE) && b.getY() == SPAWN_Y_LEVEL)) {
							if (queued.add(b)) {
								blocks++;
							}
						}
					}
				}
			}
			if ((x == z) || ((x < 0) && (x == -z)) || ((x > 0) && (x == 1 - z))) {
				t = dx;
				dx = -dz;
				dz = t;
			}
			x += dx;
			z += dz;
		}
		this.blocksToChange = blocks;
		return blocks;
	}

	/**
	 * If populate() was called, returns the amount of blocks that will be
	 * changed in the next clear
	 * 
	 * @return blocks to change
	 */
	public int getBlocksToChange() {
		return blocksToChange;
	}

	/**
	 * Gets the amount of blocks that have been changed
	 * 
	 * @return blocks changed
	 */
	public int getBlocksChanged() {
		return blocksChanged.get();
	}

	/**
	 * Gets the timestamp of when the spawn clearing tast started
	 * 
	 * @return timeStarted
	 */
	public long getTaskStarted() {
		return taskStarted;
	}

	/**
	 * Gets the amount of passes done
	 * 
	 * @return passes
	 */
	public int getPasses() {
		return passes;
	}

	/**
	 * Gets the amount of passes that the task will be completed in
	 * 
	 * @return passes
	 */
	public int getPassesNeeded() {
		int blocks = queued.size();
		int passes = 0;
		passes += (blocks / MAX_BLOCK_CHANGES_PER_PASS);
		if (blocks % MAX_BLOCK_CHANGES_PER_PASS != 0) {
			passes++;
		}
		return passes;
	}

	/**
	 * Gets the time that the task might be finished around
	 * 
	 * @return time
	 */
	public long getEstimatedFinishTime() {
		return (System.currentTimeMillis() + (getPassesNeeded() * 150));
	}

	/**
	 * Gets the main area of the stone region for the spawn
	 * 
	 * @return main stone region
	 */
	public static Cuboid getMainArea() {
		return mainArea;
	}

}
