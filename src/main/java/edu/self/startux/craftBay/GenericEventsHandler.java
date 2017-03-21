package edu.self.startux.craftBay;

import com.winthier.generic_events.GenericEventsPlugin;
import org.bukkit.inventory.ItemStack;

public final class GenericEventsHandler {
    public String getItemName(ItemStack item) {
        return GenericEventsPlugin.getInstance().getItemName(item);
    }
}
