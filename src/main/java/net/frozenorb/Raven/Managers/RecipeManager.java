package net.frozenorb.Raven.Managers;

import net.frozenorb.Raven.RavenPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@SuppressWarnings("deprecation")
public class RecipeManager {
	private ShapelessRecipe xpRecipe;

	/**
	 * Gets the instance of the bottled xp recipe
	 * 
	 * @return bottled xp recipe
	 */
	public Recipe getBottledXPRecipe() {
		return xpRecipe;
	}

	/**
	 * Initializes the soup and exp bottle recipes
	 */
	public void setupRecipes() {
		ItemStack potion = new ItemStack(Material.EXP_BOTTLE);
		ItemMeta meta = potion.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + "Bottled XP");
		potion.setItemMeta(meta);
		xpRecipe = new ShapelessRecipe(potion);
		xpRecipe.addIngredient(1, Material.GLASS_BOTTLE);
		RavenPlugin.get().getServer().addRecipe(xpRecipe);

		ShapelessRecipe grinderRecipe = new ShapelessRecipe(new ItemStack(Material.MUSHROOM_SOUP)).addIngredient(Material.INK_SACK, (byte) 3).addIngredient(Material.BOWL);
		RavenPlugin.get().getServer().addRecipe(grinderRecipe);

		ItemStack i = new ItemStack(Material.POTION);

		PotionMeta pm = (PotionMeta) i.getItemMeta();
		pm.setMainEffect(PotionEffectType.FAST_DIGGING);
		pm.addCustomEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, (int) (90 * 20L), 1), false);

		pm.setDisplayName("Potion of Haste II");

		i.setItemMeta(pm);

		ShapelessRecipe hasteRecipe = new ShapelessRecipe(i).addIngredient(5, Material.DIAMOND).addIngredient(1, Material.DIAMOND_PICKAXE).addIngredient(2, Material.GOLD_PICKAXE).addIngredient(Material.POTION);
		//RavenPlugin.get().getServer().addRecipe(hasteRecipe);
	}
}
