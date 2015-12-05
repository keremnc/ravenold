package net.frozenorb.Raven.Types;

import lombok.Data;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

@Data
public class XPBottle {
    private ItemStack bottle;
    private int xp;

    public XPBottle(int xp) {
        this(1, xp);
    }

    public XPBottle(int amount, int xp) {
        this.bottle = new ItemStack(Material.EXP_BOTTLE, amount);
        this.xp = xp;
    }

    public ItemStack create() {
        ItemMeta meta = bottle.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + "Bottled XP");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.BLUE + "Right click to get all of the bottled xp.");
        lore.add(ChatColor.BLUE + "XP: " + ChatColor.WHITE + NumberFormat.getInstance().format(getXp()));
        meta.setLore(lore);
        bottle.setItemMeta(meta);
        return bottle;
    }


}
