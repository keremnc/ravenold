package net.frozenorb.Raven.Types;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Team.Team;
import net.frozenorb.mBasic.Basic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TimeZone;

public class PlayerScoreboardUpdater {
	private int helmet, chest, legs, boots = 0;
	@SuppressWarnings("unused") private int maxh, maxc, maxl, maxb = 0;
	private String name;
	private Player p;
	private boolean initialized;

	private HashMap<Integer, String> currentlyBeingDisplayed = new HashMap<Integer, String>();

	private static final PotionEffectType[] NEGATIVE_POTION_EFFECTS = {
			PotionEffectType.SLOW, PotionEffectType.POISON,
			PotionEffectType.WITHER, PotionEffectType.WEAKNESS };

	public PlayerScoreboardUpdater(Player p) {
		this.p = p;
		this.name = p.getName();

	}

	public int getBootsDurability() {
		return boots;
	}

	public int getChestplateDurability() {
		return chest;
	}

	public int getHelmemDurability() {
		return helmet;
	}

	public int getLeggingsDurability() {
		return legs;
	}

	private int getDurability(ItemStack i) {
		if (i == null)
			return 0;
		return i.getType().getMaxDurability() - i.getDurability();
	}

	private int getMaxDurability(ItemStack i) {
		if (i == null)
			return 0;
		return i.getType().getMaxDurability();
	}

	private static String capitalizeString(String string) {
		char[] chars = string.toLowerCase().toCharArray();
		boolean found = false;
		for (int i = 0; i < chars.length; i++) {
			if (!found && Character.isLetter(chars[i])) {
				chars[i] = Character.toUpperCase(chars[i]);
				found = true;
			} else if (Character.isWhitespace(chars[i]) || chars[i] == '.' || chars[i] == '\'') {
				found = false;
			}
		}
		return String.valueOf(chars).replace("_", " ");
	}

	public boolean hasArmor() {
		return (helmet != 0 || chest != 0 || legs != 0 || boots != 0);
	}

	public void updateItems() {
		PlayerInventory i = p.getInventory();
		helmet = getDurability(i.getHelmet());
		chest = getDurability(i.getChestplate());
		legs = getDurability(i.getLeggings());
		boots = getDurability(i.getBoots());

		maxh = getMaxDurability(i.getHelmet());
		maxc = getMaxDurability(i.getChestplate());
		maxl = getMaxDurability(i.getLeggings());
		maxb = getMaxDurability(i.getBoots());
	}

	public void update() {
		updateItems();
		Scoreboard sb = p.getScoreboard();
		Objective o = sb.getObjective("no");
		if (o == null) {
			o = sb.registerNewObjective("no", "offense");
		}
		o.setDisplaySlot(DisplaySlot.SIDEBAR);
		o.setDisplayName("§6§lMCTeams Map 2");
		HashMap<Integer, String> lines = getLines();
		removeOldLines(lines);
		currentlyBeingDisplayed = lines;
		for (Entry<Integer, String> entry : lines.entrySet()) {
			Score s = o.getScore(entry.getValue());
			s.setScore(entry.getKey());
		}
	}

	public void removeOldLines(HashMap<Integer, String> lines) {
		Scoreboard sb = p.getScoreboard();
		for (Integer key : currentlyBeingDisplayed.keySet()) {
			String current = currentlyBeingDisplayed.get(key);
			if (lines.containsKey(key)) {
				if (!lines.get(key).equals(current)) {
					sb.resetScores(currentlyBeingDisplayed.get(key));
				}
			} else {
				sb.resetScores(currentlyBeingDisplayed.get(key));

			}
		}
	}

	public HashMap<Integer, String> getLines() {
		HashMap<Integer, String> lines = new HashMap<Integer, String>();
		try {
			int first = 15;
//			if (hasArmor()) {
//				lines.put(first, "§a§lArmor");
//				first -= 1;
//				for (Field f : this.getClass().getDeclaredFields()) {
//					if (f.getType().isPrimitive()) {
//						f.setAccessible(true);
//						if (f.getType().getName().equals(boolean.class.getName())) {
//							continue;
//						}
//						if (f.getName().startsWith("max"))
//							continue;
//						int val = f.getInt(this);
//						if (val != 0) {
//							String fieldName = capitalizeString(f.getName());
//							String wantedField = "max" + f.getName().substring(0, 1);
//							Field fmax = this.getClass().getDeclaredField(wantedField);
//							fmax.setAccessible(true);
//							int max = fmax.getInt(this);
//							double percentage = ((double) val) / ((double) max);
//							int perc = (int) (percentage * 100);
//							if (percentage > .5) {
//								fieldName = "  §a" + fieldName + "  " + perc + "%";
//							} else if (percentage > .25) {
//								fieldName = "  §6" + fieldName + "  " + perc + "%";
//							} else {
//								fieldName = "  §c" + fieldName + "  " + perc + "%";
//
//							}
//							lines.put(first, fieldName);
//							first -= 1;
//						}
//					}
//				}
//				lines.put(first, StringUtils.repeat(" ", first));
//				first -= 1;
//			}
//			if (p.getActivePotionEffects().size() > 0) {
//				lines.put(first, "§e§lPotions");
//				first -= 1;
//
//				List<PotionEffect> pots = Arrays.asList(p.getActivePotionEffects().toArray(new PotionEffect[] {}));
//				Collections.sort(pots, new Comparator<PotionEffect>() {
//					@Override
//					public int compare(PotionEffect arg0, PotionEffect arg1) {
//						return ((Integer) arg0.getDuration()).compareTo(arg1.getDuration());
//					}
//				});
//				int currentpots = 0;
//				for (PotionEffect pot : pots) {
//					currentpots += 1;
//					if (currentpots > 3)
//						break;
//					String fpe = FriendlyPotionEffect.getName(pot.getType());
//					boolean neg = Arrays.asList(NEGATIVE_POTION_EFFECTS).contains(pot.getType());
//					String color = neg ? "§c" : "§a";
//					lines.put(first, "  " + color + fpe + " " + (pot.getAmplifier() + 1));
//					first -= 1;
//					StringBuilder str = new StringBuilder();
//					for (int i = 0; i < currentpots; i += 1)
//						str.append(StringUtils.repeat(" ", i));
//					lines.put(first, "     " + ((pot.getDuration() / 20) < 10 ? "§c" + getFormatted(pot.getDuration() / 20) : "§b" + getFormatted(pot.getDuration() / 20)) + str.toString());
//					first -= 1;
//
//				}
//
//				lines.put(first, StringUtils.repeat(" ", first));
//				first -= 1;
//			}
			if (first > 2) {
				lines.put(first, "§e§lGold");
				first -= 1;
				lines.put(first, "  " + ((int) Basic.get().getUuidEconomyAccess().getBalance(p.getUniqueId())));
				first -= 1;
                lines.put(first, "§e§lTeam");
                first -= 1;
                Team t = RavenPlugin.get().getTeamManager().getPlayerTeam(p.getUniqueId());
                lines.put(first, "  " + (t == null ? "No Team" : t.getFriendlyName()));
			}
		} catch (IllegalArgumentException | SecurityException ex) {
			ex.printStackTrace();
		}
		return lines;
	}

	private String getFormatted(int val) {
		int millis = val * 1000;
		TimeZone tz = TimeZone.getTimeZone("UTC");
		SimpleDateFormat df = new SimpleDateFormat("m:ss");
		df.setTimeZone(tz);
		String time = df.format(new Date(millis));
		return time;
	}

	/**
	 * Updates a player's scoreboard
	 * 
	 * @param p
	 *            the player to update
	 */

	/**
	 * Loads a player's scoreboard for the first time
	 * 
	 * @param player
	 *            the player to load to
	 */
	public void loadScoreboard(Player player) {
		Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective o = sb.registerNewObjective("no", "offense");
		o.setDisplaySlot(DisplaySlot.SIDEBAR);
		o.setDisplayName(player.getDisplayName());
		player.setScoreboard(sb);
		initialized = true;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	private enum FriendlyPotionEffect {
		FIRE_RESISTANCE(PotionEffectType.FIRE_RESISTANCE, "Fire Res"),
		INCREASE_DAMAGE(PotionEffectType.INCREASE_DAMAGE, "Strength"),
		SPEED(PotionEffectType.SPEED, "Speed"),
		NIGHT_VISION(PotionEffectType.NIGHT_VISION, "N Vision"),
		WATER_BREATHING(PotionEffectType.WATER_BREATHING, "W Breath"),
		INVISIBILITY(PotionEffectType.INVISIBILITY, "Invis"),
		REGENERATION(PotionEffectType.REGENERATION, "Regen");
		private PotionEffectType pot;
		private String name;

		FriendlyPotionEffect(PotionEffectType pot, String name) {
			this.pot = pot;
			this.name = name;
		}

		public PotionEffectType getPotionEffectType() {
			return pot;
		}

		public String getName() {
			return name;
		}

		public static String getName(PotionEffectType o) {
			for (FriendlyPotionEffect f : values()) {
				if (f.getPotionEffectType().getName().equals(o.getName())) {
					return f.getName();
				}
			}
			String type = o.getName();
			if (type.length() > 10) {
				type = type.substring(0, 10);
			}
			return capitalizeString(type);
		}
	}

}
