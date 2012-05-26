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

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.Listener;

public class AuctionHouse implements Listener {
        private CraftBayPlugin plugin;
        private long lastUID;

        public AuctionHouse(CraftBayPlugin plugin) {
                this.plugin = plugin;
        }

        public void enable() {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        public Auction createAuction(Merchant owner, Item item, int startingBid) {
                if (!plugin.getAuctionScheduler().canQueue()) {
                        owner.warn(plugin.getLocale().getMessage("auction.create.QueueFull").set(owner));
                        return null;
                }
                if (!item.has(owner)) {
                        owner.warn(plugin.getLocale().getMessage("auction.create.NotEnoughItems"));
                        return null;
                }
                int fee = plugin.getConfig().getInt("auctionfee");
                if (startingBid > plugin.getConfig().getInt("startingbid")) {
                        int tax = (plugin.getConfig().getInt("auctiontax") * (startingBid - plugin.getConfig().getInt("startingbid"))) / 100;
                        fee += tax;
                }
                if (fee > 0) {
                        if (!owner.hasAmount(fee)) {
                                owner.warn(plugin.getLocale().getMessage("auction.create.FeeTooHigh").set(owner).set("fee", new MoneyAmount(fee)));
                                return null;
                        }
                        owner.takeAmount(fee);
                        owner.msg(plugin.getLocale().getMessage("auction.create.FeeDebited").set(owner).set("fee", new MoneyAmount(fee)));
                }
                item = item.take(owner);
                Auction auction = new TimedAuction(plugin, owner, item);
                long uid = System.currentTimeMillis();
                if (uid == lastUID) uid += 1;
                lastUID = Math.max(uid, lastUID);
                auction.setUID(uid);
                auction.setState(AuctionState.QUEUED);
                if (startingBid != 0) auction.setStartingBid(startingBid);
                plugin.getAuctionScheduler().queueAuction(auction);
                return auction;
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerJoin(PlayerJoinEvent event) {
                ItemDelivery.deliverAll();
        }
}