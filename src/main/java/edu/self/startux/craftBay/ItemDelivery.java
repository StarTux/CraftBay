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

import edu.self.startux.craftBay.locale.Message;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

public class ItemDelivery implements ConfigurationSerializable {
    private Merchant recipient;
    private Item item;
    private Auction auction;
    private Date creationDate;
    private transient long lastReminder;

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

    public void remind(Player player) {
        if (!player.hasPermission("auction.receive")) return;
        long now = System.currentTimeMillis();
        if (lastReminder > now - 60000L) return;
        lastReminder = now;
        Message message = CraftBayPlugin.getInstance().getMessage("delivery.Reminder").set(recipient);
        Message tooltip = CraftBayPlugin.getInstance().getMessage("delivery.ReminderTooltip").set(recipient);
        CraftBayPlugin.getInstance().msg(player, message.compile()
                                         .clickEvent(ClickEvent.runCommand("/auc deliver"))
                                         .hoverEvent(HoverEvent.showText(tooltip.compile())));
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
     * @return The ItemDelivery instance
     */
    public static ItemDelivery schedule(Merchant recipient, Item item, Auction auction) {
        ItemDelivery delivery = new ItemDelivery(recipient, item, auction, new Date());
        CraftBayPlugin.getInstance().getAuctionScheduler().queueDelivery(delivery);
        return delivery;
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

    public void deliver(Player player) {
        CraftBayPlugin.getInstance().getAuctionInventory().initDelivery(player, item.toItemStackList());
    }
}
