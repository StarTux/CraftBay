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

public class AuctionHouse {
        private CraftBayPlugin plugin;

        public AuctionHouse(CraftBayPlugin plugin) {
                this.plugin = plugin;
        }

        public void enable() {
        }

        public Auction createAuction(Merchant owner, Item item) {
                if (!item.has(owner)) {
                        owner.warn("You do not have enough " + item.getName() + "!");
                        return null;
                }
                item = item.take(owner);
                return new TimedAuction(plugin, owner, item);
        }
}