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

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class ItemDelivery implements ConfigurationSerializable {
        private Merchant recipient;
        private Item item;
        private Auction auction;
        private Date creationDate;
        private long attempt;

        public ItemDelivery(Merchant recipient, Item item, Auction auction, Date creationDate) {
                this.recipient = recipient;
                this.item = item;
                this.auction = auction;
                this.creationDate = creationDate;
        }

        public String getLocation() {
                if (!(recipient instanceof PlayerMerchant)) {
                        return "none";
                }
                Location loc = ((PlayerMerchant)recipient).getPlayer().getLocation();
                return String.format("%s (%d,%d,%d)", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }

        public boolean deliver() {
                attempt++;
                final boolean debugMode = CraftBayPlugin.getInstance().getDebugMode();
                if (recipient instanceof PlayerMerchant) {
                        PlayerMerchant player = (PlayerMerchant)recipient;
                        if (player.getPlayer() != null) {
                                if (!player.getPlayer().hasPermission("auction.receive")) {
                                        if (debugMode && attempt == 1) {
                                                CraftBayPlugin.getInstance().getLogger().info(String.format("DELIVER FAIL item='%s' recipient='%s' location='%s' reason='No Permission'", item.toString(), recipient.getName(), getLocation()));
                                        }
                                        return false;
                                }
                                if (CraftBayPlugin.getInstance().getBlacklistWorlds().contains(player.getPlayer().getWorld().getName())) {
                                        if (debugMode && attempt == 1) {
                                                CraftBayPlugin.getInstance().getLogger().info(String.format("DELIVER FAIL item='%s' recipient='%s' location='%s' reason='World Blacklisted'", item.toString(), recipient.getName(), getLocation()));
                                        }
                                        return false;
                                }
                        }
                }
                boolean result = item.give(recipient);
                if (result) {
                        String msg = String.format("DELIVER item='%s' recipient='%s' location='%s'", item.toString(), recipient.getName(), getLocation());
                        if (auction != null) {
                                auction.log(msg);
                        } else {
                                CraftBayPlugin.getInstance().getLogger().info(msg);
                        }
                } else {
                        String msg = String.format("DELIVER FAIL item='%s' recipient='%s' reason='Player offline'", item.toString(), recipient.getName());
                        if (auction != null) {
                                if (debugMode && attempt == 1) auction.log(msg);
                        } else {
                                if (debugMode && attempt == 1) CraftBayPlugin.getInstance().getLogger().info(msg);
                        }
                }
                return result;
        }

        public Item getItem() {
                return item;
        }

        public Merchant getRecipient() {
                return recipient;
        }

        public Date getCreationDate() {
                return new Date(creationDate.getTime());
        }

        /**
         * Schedule a delivery as soon as possible, even immediately.
         * @param recipient the recipient
         * @param item the item
         * @param auction the associated Auction instance
         * @return The ItemDelivery instance if it had to be
         * scheduled for the future, null otherwise
         */
        public static ItemDelivery schedule(Merchant recipient, Item item, Auction auction) {
                ItemDelivery delivery = new ItemDelivery(recipient, item, auction, new Date());
                if (!delivery.deliver()) {
                        CraftBayPlugin.getInstance().getAuctionScheduler().queueDelivery(delivery);
                        return delivery;
                }
                return null;
        }

        public static void deliverAll() {
                CraftBayPlugin.getInstance().getAuctionScheduler().checkDeliveries();
        }

        public Map<String, Object> serialize() {
                Map<String, Object> result = new HashMap<String, Object>();
                result.put("recipient", recipient.clone());
                result.put("item", item.clone());
                result.put("created", creationDate.getTime());
                return result;
        }

        public static ItemDelivery deserialize(Map<String, Object> map) {
                Merchant recipient = (Merchant)map.get("recipient");
                Item item = (Item)map.get("item");
                Auction auction = null;
                Date creationDate = new Date((Long)map.get("created"));
                return new ItemDelivery(recipient, item, auction, creationDate);
        }
}
