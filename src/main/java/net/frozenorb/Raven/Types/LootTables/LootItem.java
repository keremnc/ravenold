package net.frozenorb.Raven.Types.LootTables;

import lombok.*;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Ryan on 5/27/2015
 * <p/>
 * Project: raven
 */
@Data
@EqualsAndHashCode
@AllArgsConstructor
@RequiredArgsConstructor
public class LootItem {
    @NonNull private ItemStack parent;
    /**
     *  The chance of the item spawning.
     *  -1 for 100% chance of spawning
     *  0 for absolute randomness (no set chance)
     */
    @NonNull private int chance;

    /**
     * The maximum amount of items that can be given
     */
    @NonNull private int maxWeight;

    private int minAmount;


    public void setAmount(int amount) {
        parent.setAmount(amount);
    }

    public int getAmount() {
        return parent.getAmount();
    }



}
