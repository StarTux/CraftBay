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

public class ItemDelivery {
        private Merchant recipient;
        private Item item;
        private Auction auction;

        public ItemDelivery(Merchant recipient, Item item, Auction auction) {
                this.recipient = recipient;
                this.item = item;
        }

        public boolean deliver() {
                boolean result = item.give(recipient);
                if (result) {
                        System.out.println("Delivery successful: " + item + " to " + recipient.getName());
                } else {
                        System.out.println("Delivery failed: " + item + " to " + recipient.getName());
                }
                return result;
        }

        public static void schedule(Merchant recipient, Item item, Auction auction) {
                ItemDelivery delivery = new ItemDelivery(recipient, item, auction);
                if (!delivery.deliver()) {
                        CraftBayPlugin.getInstance().getAuctionScheduler().queueDelivery(delivery);
                }
        }
}