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
import edu.self.startux.craftBay.Merchant;
import edu.self.startux.craftBay.MoneyAmount;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called whenever an auction changes due to a bid.
 */
public class AuctionBidEvent extends AuctionEvent {
        private static HandlerList handlers = new HandlerList();
        private Merchant bidder, oldWinner;
        private MoneyAmount amount, oldPrice;

        public AuctionBidEvent(Auction auction, Merchant bidder, MoneyAmount amount, Merchant oldWinner, MoneyAmount oldPrice) {
                super(auction);
                this.bidder = bidder;
                this.amount = amount;
                this.oldWinner = oldWinner;
                this.oldPrice = oldPrice;
        }

        public static HandlerList getHandlerList() {
                return handlers;
        }

        @Override
        public HandlerList getHandlers() {
                return handlers;
        }

        public Merchant getBidder() {
                return bidder;
        }

        public MoneyAmount getAmount() {
                return amount;
        }

        public Merchant getOldWinner() {
                return oldWinner;
        }

        public MoneyAmount getOldPrice() {
                return oldPrice;
        }
}
