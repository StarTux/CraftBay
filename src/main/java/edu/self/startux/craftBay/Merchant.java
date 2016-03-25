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

import edu.self.startux.craftBay.locale.Message;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

/**
 * This class is an abstract representation of a participant in an
 * auction, either as auctioneer or as bidder.
 * Typical actions are the giving, taking away and checking for
 * availability of items and money.
 * Primary motif for creating this abstraction is to manage both
 * players and The Bank in the same way.
 */
public interface Merchant extends ConfigurationSerializable {
        /**
         * Get the name of this Merchant.
         * @return the name
         */
        public String getName();

        /**
         * Check if this Merchant has a given amount
         * @param amount the amount
         * @return true if he has enough, false otherwise
         */
        public boolean hasAmount(MoneyAmount amount);

        /**
         * Add some money to this merchant's account.
         * @param amount the amount
         * @return transaction success
         */
        public boolean giveAmount(MoneyAmount amount);

        /**
         * Debit this merchant's account.
         * @param amount the amount
         * @return transaction success
         */
        public boolean takeAmount(MoneyAmount amount);

        /**
         * Check this merchant's inventory for availabilty of an
         * ItemStack.
         * @param lot the ItemStack
         * @return true if he as it, false otherwise
         */
        public boolean hasItem(ItemStack lot);

        /**
         * Remove an ItemStack from this merchant's inventory.
         * @param lot the ItemStack
         * @return true if the item could be taken, false otherwise
         */
        public boolean takeItem(ItemStack lot);

        /**
         * Check if this merchant has a Bukkit permission.
         * @param permission the permission
         * @return true if he has it, false otherwise
         */
        public boolean hasPermission(String permission);

        /**
         * Send a message to this merchant.
         * @param msg the message
         */
        public void msg(Message msg);

        /**
         * Send a warning to this merchant.
         * @param msg the warning
         */
        public void warn(Message msg);

        public boolean isListening();
        public Merchant clone();
}
