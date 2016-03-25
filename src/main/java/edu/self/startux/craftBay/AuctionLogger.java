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

import edu.self.startux.craftBay.event.AuctionBidEvent;
import edu.self.startux.craftBay.event.AuctionCancelEvent;
import edu.self.startux.craftBay.event.AuctionCreateEvent;
import edu.self.startux.craftBay.event.AuctionEndEvent;
import edu.self.startux.craftBay.event.AuctionStartEvent;
import edu.self.startux.craftBay.event.AuctionTimeChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AuctionLogger implements Listener {
        private CraftBayPlugin plugin;

        public AuctionLogger(CraftBayPlugin plugin) {
                this.plugin = plugin;
        }

        public void enable() {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onAuctionCreate(AuctionCreateEvent event) {
                if (!plugin.getDebugMode()) return;
                Auction auction = event.getAuction();
                auction.log(String.format("CREATE owner='%s' item='%s' minbid='%s' fee='%s'", auction.getOwner().getName(), auction.getItem().toString(), auction.getMinimalBid(), auction.getFee()));
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onAuctionStart(AuctionStartEvent event) {
                Auction auction = event.getAuction();
                auction.log(String.format("START owner='%s' item='%s' minbid='%s' fee='%s'", auction.getOwner().getName(), auction.getItem().toString(), auction.getMinimalBid(), auction.getFee()));
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onAuctionBid(AuctionBidEvent event) {
                Auction auction = event.getAuction();
                auction.log(String.format("BID bidder='%s' amount='%s'", event.getBidder().getName(), event.getAmount()));
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onAuctionEnd(AuctionEndEvent event) {
                Auction auction = event.getAuction();
                auction.log(String.format("END winner='%s' price='%s' paymentError='%b'", (auction.getWinner() != null ? auction.getWinner().getName() : "none"), auction.getWinningBid(), event.hasPaymentError()));
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onAuctionCancel(AuctionCancelEvent event) {
                Auction auction = event.getAuction();
                auction.log(String.format("CANCEL sender='%s' owner='%s' item='%s' minbid='%s' fee='%s'", event.getSender().getName(), auction.getOwner().getName(), auction.getItem().toString(), auction.getMinimalBid(), auction.getFee()));
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onAuctionTimeChange(AuctionTimeChangeEvent event) {
                Auction auction = event.getAuction();
                auction.log(String.format("TIME sender='%s' delay='%d'", event.getSender().getName(), event.getDelay()));
        }
}
