/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright 2012-2021 StarTux
 *
 * This file is part of CraftBay.
 *
 * CraftBay is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CraftBay is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CraftBay.  If not, see <http://www.gnu.org/licenses/>.
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package edu.self.startux.craftBay;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

/**
 * This class represents an item that is for auction.
 */
public interface Item extends ConfigurationSerializable {
    /**
     * Get the simple name.
     * @return the name
     */
    Component getName();
    Component getDescription();

    ItemAmount getAmount();
    Component getItemInfo();

    /**
     * Check if a merchant has this item.
     * @param merchant the merchant
     * @return true if the merchant has enough, false otherwise
     */
    boolean has(Merchant merchant);

    /**
     * Produce a list of item stacks.
     */
    List<ItemStack> toItemStackList();

    Item clone();

    Component toComponent();

    default String getStringName() {
        return ChatColor.stripColor(PlainTextComponentSerializer.plainText().serialize(getName()));
    }
}
