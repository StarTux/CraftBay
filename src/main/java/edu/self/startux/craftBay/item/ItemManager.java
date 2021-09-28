package edu.self.startux.craftBay.item;

import edu.self.startux.craftBay.CraftBayPlugin;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

/**
 * An Item Manager is registered with CraftBayPlugin and allows to
 * treat ItemStacks as non-vanilla items. Effectively, this means that
 * an item can have a custom name.
 */
public interface ItemManager {
    /**
     * Does this manager manage this item exclusively?
     */
    boolean isManaged(ItemStack itemStack);

    /**
     * Produce the item display name.
     */
    Component getDisplayName(ItemStack itemStack);

    /**
     * Detailed item info.
     */
    Component getItemInfo(ItemStack itemStack);

    static List<ItemManager> getList(CraftBayPlugin plugin) {
        List<ItemManager> result = new ArrayList<>();
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("Mytems")) {
                if (Bukkit.getPluginManager().getPlugin("Mytems").getClass().getName().equals("com.cavetale.mytems.MytemsPlugin")) {
                    result.add(new MytemsItemManager());
                    plugin.getLogger().info("Mytems plugin found!");
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }
}
