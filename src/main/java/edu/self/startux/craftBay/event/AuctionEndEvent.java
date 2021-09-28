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

package edu.self.startux.craftBay.event;

import edu.self.startux.craftBay.Auction;
import org.bukkit.event.HandlerList;

/**
 * Called whenever an auction ends and an item and maybe money
 * will go to merchants.
 */
public class AuctionEndEvent extends AuctionEvent {
        private static HandlerList handlers = new HandlerList();
        private boolean paymentError = false;

        public AuctionEndEvent(Auction auction) {
                super(auction);
        }

        public static HandlerList getHandlerList() {
                return handlers;
        }

        @Override
        public HandlerList getHandlers() {
                return handlers;
        }

        public void setPaymentError(boolean paymentError) {
                this.paymentError = paymentError;
        }

        public boolean hasPaymentError() {
                return paymentError;
        }
}
