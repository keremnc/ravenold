package net.frozenorb.Raven.Tasks;

import net.frozenorb.Raven.Types.Cuboid;
import net.frozenorb.Raven.Types.LootTables.EndLootTable;
import net.frozenorb.Utilities.DataSystem.Regioning.CuboidRegion;
import net.frozenorb.Utilities.DataSystem.Regioning.RegionManager;
import org.bukkit.Chunk;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryan on 6/17/2015
 * <p/>
 * Project: raven
 */
public class ChestRefillTask extends BukkitRunnable {

    EndLootTable table;

    List<Chest> chests;

    @Override
    public void run() {
        CuboidRegion endRegion = RegionManager.get().getByName("end");
        if (endRegion != null) {
            Cuboid cube = new Cuboid(endRegion.getMinimumPoint(), endRegion.getMaximumPoint());
            chests = new ArrayList<>();

            for (Chunk chunk : cube.getChunks()) {
                for (BlockState te : chunk.getTileEntities()) {
                    if (te instanceof Chest) {
                        Chest chest = (Chest) te;
                        chest.getBlockInventory().clear();
                        chests.add(chest);
                    }
                }
            }

            table = new EndLootTable(chests);
            for (Chest ch : chests) {
                table.populate(ch.getBlockInventory());
                ch.update();
            }
            table.cleanup();
        }

    }
}
