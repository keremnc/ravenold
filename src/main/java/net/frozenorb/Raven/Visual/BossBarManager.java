package net.frozenorb.Raven.Visual;

import net.frozenorb.Utilities.Types.Scrollable;
import net.minecraft.server.v1_7_R4.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Class that handles the setting of the boss health bar, as well as timing it.
 *
 * @author Kerem Celik
 */
@SuppressWarnings("deprecation")
public class BossBarManager extends BukkitRunnable {
    private static final int ENTITY_ID_MODIFIER = 1236912369;

    private HashMap<String, Scrollable> messages = new HashMap<String, Scrollable>();

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {

//            boolean setBar = true;

//            KOTH end;
//
//            if ((end = RavenPlugin.get().getKOTHHandler().getKOTH("End")) != null) {
//                if (end.isActive()) {
//                    setBar = false;
//                }
//            } else if ((end = RavenPlugin.get().getKOTHHandler().getKOTH("EndEvent")) != null) {
//                   if (end.isActive()) {
//                       setBar = false;
//                   }
//            }

                setBar(p);

            boolean hasArmor = false;

            for (ItemStack is : p.getInventory().getArmorContents()) {
                if (is != null && is.getType() != Material.AIR) {
                    hasArmor = true;
                }
            }
            if (p.getWorld().getEnvironment() == Environment.THE_END && !hasArmor && p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                p.removePotionEffect(PotionEffectType.INVISIBILITY);
                p.sendMessage(ChatColor.RED + "You need at least one piece of armor to be invisible in The End!");
            }
        }
    }

    /**
     * Updates the boss bar of a player, and sets to the current string
     *
     * @param p the player to set to
     */
    public void setBar(Player p) {
        if (messages.containsKey(p.getName())) {
            Scrollable display = messages.get(p.getName());

            String d = display.next();

            if (d.startsWith("~")) {

                unregisterPlayer(p);
                return;
            }
            spawnNewPlate(p, d, 200);
        }
    }

    public void setBar(Player p, int health) {
        if (messages.containsKey(p.getName())) {
            Scrollable display = messages.get(p.getName());

            String d = display.next();

            if (d.startsWith("~")) {

                unregisterPlayer(p);
                return;
            }
            spawnNewPlate(p, d, health);
        }

    }

    /**
     * Adds the scrollable to the player, and sets their bar.
     *
     * @param player     the player to register String to
     * @param scrollable the scrollable to register
     */
//	public void registerMessage(Player player, Scrollable scrollable) {
//		messages.put(player.getName(), scrollable);
//		PacketPlayOutEntityDestroy pac = new PacketPlayOutEntityDestroy(player.getEntityId() + ENTITY_ID_MODIFIER);
//		((CraftPlayer) player).getHandle().playerConnection.sendPacket(pac);
//		setBar(player);
//	}
    public void registerMessage(Player player, Scrollable scrollable, int health) {
        messages.put(player.getName(), scrollable);
        PacketPlayOutEntityDestroy pac = new PacketPlayOutEntityDestroy(player.getEntityId() + ENTITY_ID_MODIFIER);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(pac);
        setBar(player, health);

    }

    /**
     * Removes a player from the bar map, and removes the bar
     *
     * @param player the player to remove the bar on
     */
    public void unregisterPlayer(Player player) {
        messages.remove(player.getName());
        PacketPlayOutEntityDestroy pac = new PacketPlayOutEntityDestroy(player.getEntityId() + ENTITY_ID_MODIFIER);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(pac);

    }

	/*
     * ------------PRIVATE PACKET METHODS---------------
	 */
//	private void spawnNewPlate(Player player, String display) {
//		displayTextBar(display, player);
//	}

    private void spawnNewPlate(Player player, String display, int health) {
        displayTextBar(display, player, health);
    }

    private void sendPacket(Player player, Packet packet) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        entityPlayer.playerConnection.sendPacket(packet);
    }
//
//	private PacketPlayOutSpawnEntityLiving getMobPacket(Player p, String text, Location loc) {
//		return getMobPacket(p, text, loc, 200);
//	}

    private PacketPlayOutSpawnEntityLiving getMobPacket(Player p, String text, Location loc, int health) {
        PacketPlayOutSpawnEntityLiving mobPacket = new PacketPlayOutSpawnEntityLiving();
        final EntityEnderDragon dragon = new EntityEnderDragon(((CraftWorld) p.getWorld()).getHandle());
//		dragon.getAttributeInstance(GenericAttributes.maxHealth).setValue(health);
        dragon.setHealth(health);

        int x = (int) Math.floor(loc.getBlockX() * 32.0D);
        int y = (int) Math.floor((loc.getBlockY() - 120) * 32.0D);
        int z = (int) Math.floor(loc.getBlockZ() * 32.0D);
        try {
			/* id */
            Field cID = mobPacket.getClass().getDeclaredField("a");
            cID.setAccessible(true);
            cID.set(mobPacket, (int) p.getEntityId() + ENTITY_ID_MODIFIER);
            cID.setAccessible(false);
			/* name */
            Field cName = mobPacket.getClass().getDeclaredField("b");
            cName.setAccessible(true);
            cName.set(mobPacket, EntityType.ENDER_DRAGON.getTypeId());
            cName.setAccessible(false);
			/* x */
            Field cF = mobPacket.getClass().getDeclaredField("c");
            cF.setAccessible(true);
            cF.set(mobPacket, x);
            cF.setAccessible(false);
			/* y */
            Field cY = mobPacket.getClass().getDeclaredField("d");
            cY.setAccessible(true);
            cY.set(mobPacket, y);
            cY.setAccessible(false);
			/* z */
            Field cZ = mobPacket.getClass().getDeclaredField("e");
            cZ.setAccessible(true);
            cZ.set(mobPacket, z);
            cZ.setAccessible(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        DataWatcher watcher = getWatcher(text, dragon, health);
        try {
            Field t = PacketPlayOutSpawnEntityLiving.class.getDeclaredField("l");
            t.setAccessible(true);
            t.set(mobPacket, watcher);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mobPacket;
    }

    private DataWatcher getWatcher(String text, Entity e, int health) {
        DataWatcher watcher = new DataWatcher(e);
        watcher.a(0, (Byte) (byte) 0x20);
        watcher.a(6, (float) health);
        watcher.a(10, (String) text);
        watcher.a(11, (Byte) (byte) 1);

        return watcher;
    }

//	private void displayTextBar(String text, final Player player) {
//		PacketPlayOutSpawnEntityLiving mobPacket = getMobPacket(player, text, player.getLocation(), 200);
//		sendPacket(player, mobPacket);
//	}

    private void displayTextBar(String text, final Player player, int percent) {
        PacketPlayOutSpawnEntityLiving mobPacket = getMobPacket(player, text, player.getLocation(), percent);
        sendPacket(player, mobPacket);
    }
}
