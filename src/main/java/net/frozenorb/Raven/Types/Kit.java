package net.frozenorb.Raven.Types;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

public class Kit {
	private ItemStack[] contents;
	private ItemStack[] armorcontents;

	private PotionEffect[] potionEffects = new PotionEffect[] {};
	private String name;

	/**
	 * Creates a new kit based on the player's inventory
	 * 
	 * @param p
	 *            the player to create the kit based on
	 * @param name
	 *            the name of the kit
	 */
	public Kit(Player p, String name) {
		this.name = name;
		contents = p.getInventory().getContents();
		armorcontents = p.getInventory().getArmorContents();
		if (p.getActivePotionEffects().size() > 0)
			potionEffects = p.getActivePotionEffects().toArray(potionEffects);
	}

	/**
	 * Gets the kit's name
	 * 
	 * @return kit name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets a list of potion effects
	 * 
	 * @return potion effects
	 */
	public PotionEffect[] getPotionEffects() {
		return potionEffects;
	}

	/**
	 * Equips the given kit to the player
	 * 
	 * @param p
	 *            the player to equip to
	 */
	public void equip(Player p) {
		p.setMaxHealth(20D);
		p.setHealth(20D);
		p.setFoodLevel(20);
		p.setLevel(0);
		p.setFireTicks(0);
		p.setExp(0.0F);
		for (PotionEffect pot : p.getActivePotionEffects())
			p.removePotionEffect(pot.getType());
		PlayerInventory inv = p.getInventory();
		inv.clear();
		inv.setArmorContents(null);
		p.getInventory().setContents(contents);
		p.getInventory().setArmorContents(armorcontents);
		for (PotionEffect pot : potionEffects)
			p.addPotionEffect(pot);
		p.sendMessage(ChatColor.GRAY + "You have been equipped with the '" + name + "' kit.");
	}

	@Override
	public String toString() {
		return name;
	}
}
