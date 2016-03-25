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
import edu.self.startux.craftBay.event.AuctionTickEvent;
import edu.self.startux.craftBay.event.AuctionTimeChangeEvent;
import edu.self.startux.craftBay.locale.Message;
import java.util.Date;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AuctionAnnouncer implements Listener {
        private CraftBayPlugin plugin;
        private int broadcastTimer;
        private int reminderTimer;
        private int reminderInterval;
        private int broadcastInterval;
        private List<Integer> countdown;

        public AuctionAnnouncer(CraftBayPlugin plugin) {
                this.plugin = plugin;
        }

        public void enable() {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        public void reloadConfig() {
                reminderInterval = plugin.getConfig().getInt("reminderinterval");
                broadcastInterval = plugin.getConfig().getInt("spamprotection");
                countdown = plugin.getConfig().getIntegerList("countdown");
        }

        private boolean announce(Message msg, boolean force, boolean noTouch) {
                if (force || broadcastTimer <= 0) {
                        plugin.broadcast(msg);
                        if (!noTouch) {
                                broadcastTimer = broadcastInterval;
                                reminderTimer = reminderInterval;
                        }
                        return true;
                }
                return false;
        }

        private boolean announce(Message msg) {
                return announce(msg, false, false);
        }

        private void announce(Message msg, Message pm, Merchant merchant, boolean force, boolean noTouch) {
                if (!announce(msg, force, noTouch) || !merchant.isListening()) {
                        merchant.msg(pm);
                }
        }

        private void announce(Message msg, Message pm, Merchant merchant) {
                announce(msg, pm, merchant, false, false);
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onAuctionTick(AuctionTickEvent event) {
                Auction auction = event.getAuction();
                for (int i : countdown) {
                        if (auction.getTimeLeft() == i) {
                                Message msg;
                                msg = plugin.getMessage(auction.getWinner() == null ? "auction.countdown.NoWinner" : "auction.countdown.Winner");
                                announce(msg.set(auction), true, true);
                                reminderTimer = reminderInterval;
                                break;
                        }
                }
                if (reminderTimer <= 0 && broadcastTimer <= 0) {
                        Message msg;
                        msg = plugin.getMessage(auction.getWinner() == null ? "auction.reminder.NoWinner" : "auction.reminder.Winner");
                        announce(msg.set(auction), true, true);
                        reminderTimer = reminderInterval;
                }
                if (broadcastTimer > 0) broadcastTimer -= 1;
                if (reminderTimer > 0) reminderTimer -= 1;
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onAuctionStart(AuctionStartEvent event) {
                Auction auction = event.getAuction();
                Message msg = plugin.getMessage("auction.start.Announce");
                announce(msg.set(auction), true, true);
                broadcastTimer = 0;
                reminderTimer = reminderInterval;
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onAuctionBid(AuctionBidEvent event) {
                Auction auction = event.getAuction();
                Merchant newWinner = auction.getWinner();
                MoneyAmount newPrice = auction.getWinningBid();
                Merchant oldWinner = event.getOldWinner();
                MoneyAmount oldPrice = event.getOldPrice();
                if (oldWinner == null || (newPrice.getDouble() != oldPrice.getDouble() && oldWinner.equals(event.getBidder()))) {
                        // overbid, first winner or overbid yourself with price change (corner case)
                        Message msg = plugin.getMessage("auction.bid.NewPrice").set(event);
                        Message pm = plugin.getMessage("auction.bid.Win").set(event);
                        announce(msg, pm, event.getBidder(), false, false);
                } else if (oldWinner.equals(event.getBidder())) {
                        // winner raises, with no change of price
                        newWinner.msg(plugin.getMessage("auction.bid.Still").set(event));
                } else if (!newWinner.equals(oldWinner)) {
                        // new beats old
                        Message msg = plugin.getMessage("auction.bid.NewWinner").set(event);
                        boolean announced = announce(msg, false, false);
                        if (!announced || !newWinner.isListening()) {
                                Message pmWinner = plugin.getMessage("auction.bid.Win").set(event);
                                newWinner.msg(pmWinner);
                        }
                        if (!announced || !oldWinner.isListening()) {
                                Message pmLoser = plugin.getMessage("auction.bid.ToLoser").set(event);
                                oldWinner.msg(pmLoser);
                        }
                } else if (newPrice.getDouble() != oldPrice.getDouble()) {
                        // underbid
                        Message msg = plugin.getMessage("auction.bid.UnderBid").set(event);
                        Message pm = plugin.getMessage("auction.bid.Fail").set(event);
                        announce(msg, pm, event.getBidder(), false, false);
                }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onAuctionEnd(AuctionEndEvent event) {
                Auction auction = event.getAuction();
                if (event.hasPaymentError()) {
                        auction.getOwner().msg(plugin.getMessage("auction.end.OwnerPaymentError").set(auction));
                        auction.getWinner().msg(plugin.getMessage("auction.end.WinnerPaymentError").set(auction));
                        plugin.broadcast(plugin.getMessage("auction.end.PaymentError").set(auction));
                } else if (event.getAuction().getWinner() == null) {
			Message msg = plugin.getMessage("auction.end.NoBid").set(auction);
                        Message pm = plugin.getMessage("auction.end.OwnerReturn").set(auction);
                        announce(msg, pm, auction.getOwner(), true, false);
                } else {
                        Message msg = plugin.getMessage("auction.end.Winner").set(auction);
                        announce(msg, true, true);
                        if (!auction.getWinner().isListening()) {
                                Message pm = plugin.getMessage("auction.end.ToWinner").set(auction);
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
                if (auction.getState() == AuctionState.RUNNING) {
                        Message msg = plugin.getMessage("auction.cancel.Announce").set(auction, event.getSender());
                        announce(msg, true, true);
                } else {
                        Message pm = plugin.getMessage("auction.cancel.ToOwner");
                        auction.getOwner().msg(pm.set(auction, event.getSender()));
                }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onAuctionTimeChange(AuctionTimeChangeEvent event) {
                Auction auction = event.getAuction();
                if (event.getDelay() == 0) {
                        Message msg = plugin.getMessage("auction.end.Manual");
                        announce(msg.set(auction, event.getSender()), true, true);
                } else {
                        Message msg = plugin.getMessage("auction.time.Change");
                        announce(msg.set(event), true, true);
                }
        }
}
