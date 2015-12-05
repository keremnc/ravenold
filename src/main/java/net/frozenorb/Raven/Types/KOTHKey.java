package net.frozenorb.Raven.Types;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.frozenorb.mBasic.Utilities.NbtFactory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ryan on 6/5/2015
 * <p/>
 *
 * This class represents a KOTH key.
 */
@Data
@AllArgsConstructor
public class KOTHKey {
    private String koth;
    private int tier;
    private long timestamp;


    /**
     * @param stack - the stack you are trying to parse.
     * @return an new instance of the class, or null if it is not properly defined
     */
    public static KOTHKey parseFromStack(ItemStack stack) {
        NbtFactory.NbtCompound compound = NbtFactory.fromItemTag(NbtFactory.getCraftItemStack(stack));
        if (stack.hasItemMeta()) {
            ItemMeta m = stack.getItemMeta();
            // if it has the display name it will be a koth key.
            if (m.hasDisplayName() && m.getDisplayName().equalsIgnoreCase(
                    ChatColor.RED + "KOTH Reward Key")) {
                String koth = "";
                int tier = 0;
                long time = 0L;

                if (compound.containsKey("KOTH")) {
                    koth = (String)compound.get("KOTH");
                }

                if (compound.containsKey("Tier")) {
                    tier = (int) compound.get("Tier");
                }
                if (compound.containsKey("Time")) {
                    time = (long) compound.get("Time");
                }

                return new KOTHKey(koth, tier, time);
            }
        }
        return null;
    }


    public ItemStack createItemStack() {
        ItemStack stack = NbtFactory.getCraftItemStack(new ItemStack(Material.GOLD_NUGGET));
        // set the tag
        String kothFormat = "§f- §bObtained from: §e{§9%s§e}";
        String tierFormat = "§f- §bLevel: §e{§9%d§e}";
        String dateFormat = "§f- §bTime: §e{§9%s§e}";

        SimpleDateFormat sdf = new SimpleDateFormat("M/d HH:mm:ss");

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(String.format(kothFormat, getKoth()));
        lore.add(String.format(tierFormat, getTier()));
        lore.add(String.format(dateFormat, sdf.format(new Date(timestamp)).replace("AM", "").replace("PM", "")));
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(lore);
        meta.setDisplayName(ChatColor.RED + "KOTH Reward Key");
        stack.setItemMeta(meta);


        NbtFactory.NbtCompound compound = NbtFactory.fromItemTag(stack);
        compound.put("KOTH", getKoth());
        compound.put("Tier", getTier());
        compound.put("Time", getTimestamp());
        NbtFactory.setItemTag(stack, compound);
        return stack;
    }


    public String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("M/d HH:mm:ss");
        return sdf.format(new Date(timestamp)).replace("AM", "").replace("PM", "");
    }

}

