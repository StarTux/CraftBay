/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright 2012 StarTux
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a fake item that is nothing but a name.
 */
public final class FakeItem implements Item {
    private String name;

    public FakeItem(final String name) {
        this.name = name;
    }

    @Override
    public Item clone() {
        return new FakeItem(name);
    }

    @Override
    public Component getName() {
        return Component.text(name);
    }

    @Override
    public Component getDescription() {
        return Component.text(name);
    }

    @Override
    public ItemAmount getAmount() {
        return new ItemAmount(1);
    }

    @Override
    public Component getItemInfo() {
        return Component.empty();
    }

    @Override
    public boolean has(Merchant merchant) {
        if (merchant instanceof BankMerchant) {
            return true;
        }
        return false;
    }

    @Override
    public List<ItemStack> toItemStackList() {
        return List.of();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("name", name);
        return result;
    }

    public static FakeItem deserialize(Map<String, Object> map) {
        return new FakeItem((String) map.get("name"));
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Component toComponent() {
        return Component.text(name);
    }
}
