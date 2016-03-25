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

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an Item in somebody's hand. You do not want to keep
 * an instance of this class around before calling take().
 */
public class HandItem extends RealItem {
        public HandItem(Player player) {
                super(player.getInventory().getItemInMainHand().clone());
        }

        public HandItem(Player player, int amount) {
                this(player);
        }

        /**
         * Same effect as superclass, but take care that the item
         * in hand is considered first.
         */
        @Override
        public Item take(Merchant merchant) {
                if (merchant instanceof PlayerMerchant) {
                        Player player = ((PlayerMerchant)merchant).getPlayer();
                        ItemStack has = player.getInventory().getItemInMainHand();
                        //merchant.msg("stack: " + stack.getAmount());
                        //merchant.msg("amount = " + amount);
                        if (has.getAmount() == stack.getAmount()) {
                                //merchant.msg("hand == amount");
                                player.getInventory().setItemInMainHand(null);
                                return new RealItem(stack);
                        }
                        if (has.getAmount() > stack.getAmount()) {
                                //merchant.msg("hand > amount");
                                has.setAmount(has.getAmount() - stack.getAmount());
                                return new RealItem(stack);
                        }
                        // has.getAmount() < stack.getAmount()
                        ItemStack remainder = stack.clone();
                        remainder.setAmount(stack.getAmount() - has.getAmount());
                        player.getInventory().setItemInMainHand(null);
                        merchant.takeItem(remainder);
                        //merchant.msg("in Hand: " + stack.getAmount());
                        //merchant.msg("Remainder: " + remainder.getAmount());
                        return new RealItem(stack);
                } else {
                        super.take(merchant);
                }
                return null;
        }
}
