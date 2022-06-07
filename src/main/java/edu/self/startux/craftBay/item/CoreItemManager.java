package edu.self.startux.craftBay.item;

import com.cavetale.core.item.ItemKinds;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

/**
 * Handle Core Items.
 */
public final class CoreItemManager implements ItemManager {
    @Override
    public boolean isManaged(ItemStack itemStack) {
        return true;
    }

    @Override
    public Component getDisplayName(ItemStack itemStack) {
        return ItemKinds.chatDescription(itemStack.asOne());
    }

    @Override
    public Component getItemInfo(ItemStack itemStack) {
        return Component.empty();
    }
}
