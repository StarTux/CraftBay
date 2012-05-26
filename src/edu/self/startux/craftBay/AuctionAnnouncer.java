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
import edu.self.startux.craftBay.event.AuctionEndEvent;
import edu.self.startux.craftBay.event.AuctionStartEvent;
import edu.self.startux.craftBay.locale.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AuctionAnnouncer implements Listener {
        private CraftBayPlugin plugin;

        public AuctionAnnouncer(CraftBayPlugin plugin) {
                this.plugin = plugin;
        }

        public void enable() {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        private boolean announce(Message msg, boolean force) {
                plugin.broadcast(msg);
                return true;
        }

        private boolean announce(Message msg) {
                return announce(msg, false);
        }

        private void announce(Message msg, Message pm, Merchant merchant, boolean force) {
                if (!announce(msg) || !merchant.isListening()) {
                        merchant.msg(pm);
                }
        }

        private void announce(Message msg, Message pm, Merchant merchant) {
                announce(msg, pm, merchant, false);
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onAuctionStart(AuctionStartEvent event) {
                Auction auction = event.getAuction();
                String[] infos = { "Header", "Owner", "Item", "NoWinner", "Time", "Help" };
                Message msg = new Message();
                for (String info : infos) msg.append(plugin.getMessage("auction.info." + info));
                msg.set(auction);
                announce(msg, true);
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onAuctionBid(AuctionBidEvent event) {
                Auction auction = event.getAuction();
                Merchant newWinner = auction.getWinner();
                int newPrice = auction.getWinningBid();
                Merchant oldWinner = event.getOldWinner();
                int oldPrice = event.getOldPrice();
                if (oldWinner == null || (newPrice != oldPrice && oldWinner.equals(event.getBidder()))) {
                        // overbid, first winner or overbid yourself with price change (corner case)
                        Message msg = plugin.getMessage("auction.announce.NewPrice").set(event);
                        Message pm = plugin.getMessage("auction.bid.Win").set(event);
                        announce(msg, pm, event.getBidder());
                } else if (!newWinner.equals(oldWinner)) {
                        // new beats old
                        Message msg = plugin.getMessage("auction.announce.NewWinner").set(event);
                        Message pm = plugin.getMessage("auction.bid.Win").set(event);
                        announce(msg, pm, event.getBidder());
                } else if (newPrice != oldPrice) {
                        // underbid
                        Message msg = plugin.getMessage("auction.announce.UnderBid").set(event);
                        Message pm = plugin.getMessage("auction.bid.Fail").set(event);
                        announce(msg, pm, event.getBidder());
                }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onAuctionEnd(AuctionEndEvent event) {
                Auction auction = event.getAuction();
                if (event.hasPaymentError()) {
                        auction.getOwner().msg(plugin.getMessage("auction.end.OwnerPaymentError").set(auction));
                        auction.getWinner().msg(plugin.getMessage("auction.end.WinnerPaymentError").set(auction));
                        plugin.broadcast(plugin.getMessage("auction.announce.PaymentError").set(auction));
                } else if (event.getAuction().getWinner() == null) {
			Message msg = plugin.getMessage("auction.announce.NoBid").set(auction);
                        Message pm = plugin.getMessage("auction.end.OwnerReturn").set(auction);
                        announce(msg, pm, auction.getOwner(), true);
                } else {
                        Message msg = plugin.getMessage("auction.announce.Winner").set(auction);
                        announce(msg, true);
                        if (!auction.getWinner().isListening()) {
                                Message pm = plugin.getMessage("auction.end.Winner").set(auction);
                                auction.getWinner().msg(pm);
                        }
                        if (!auction.getOwner().isListening()) {
                                Message pm = plugin.getMessage("auction.end.OwnerSell").set(auction);
                                auction.getOwner().msg(pm);
                        }
                }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onAuctionCancel(AuctionCancelEvent event) {
                Auction auction = event.getAuction();
                Message msg = plugin.getMessage("auction.announce.Cancel").set(auction, event.getSender());
                Message pm = plugin.getMessage("auction.cancel.ToOwner").set(auction, event.getSender());
                announce(msg, pm, auction.getOwner(), true);
        }
}