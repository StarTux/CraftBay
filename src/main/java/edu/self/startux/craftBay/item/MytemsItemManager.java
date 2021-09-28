package edu.self.startux.craftBay.item;

import com.cavetale.mytems.Mytems;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

/**
 * Handle Mytems.
 */
public final class MytemsItemManager implements ItemManager {
    @Override
    public boolean isManaged(ItemStack itemStack) {
        return Mytems.forItem(itemStack) != null;
    }

    @Override
    public Component getDisplayName(ItemStack itemStack) {
        Mytems mytems = Objects.requireNonNull(Mytems.forItem(itemStack));
        return mytems.getMytem().getDisplayName(itemStack);
    }

    @Override
    public Component getItemInfo(ItemStack itemStack) {
        return Component.empty();
    }
}
