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

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AuctionInventory implements Listener {
        private CraftBayPlugin plugin;
        private Map<String, PlayerData> playerData = new HashMap<String, PlayerData>();

        public AuctionInventory(CraftBayPlugin plugin) {
                this.plugin = plugin;
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        public Inventory getInventory(Player player) {
                PlayerData data = playerData.get(player.getName());
                if (data == null) return null;
                return data.inventory;
        }

        public MoneyAmount getMinbid(Player player) {
                PlayerData data = playerData.get(player.getName());
                if (data == null) return new MoneyAmount(0.0);
                return data.minbid;
        }

        public void setPlayer(Player player, MoneyAmount minbid, Inventory inventory) {
                PlayerData data = playerData.get(player.getName());
                if (data == null) {
                        data = new PlayerData(minbid, inventory);
                        playerData.put(player.getName(), data);
                } else {
                        data.minbid = minbid;
                        data.inventory = inventory;
                }
        }

        public void initPlayer(Player player, MoneyAmount minbid) {
                String title = plugin.getMessage("auction.gui.ChestTitle").toString();
                if (title.length() > 32) title = title.substring(0, 32);
                Inventory inventory = Bukkit.createInventory(player, 54, title); 
                player.openInventory(inventory);
                setPlayer(player, minbid, inventory);
        }

        public void deletePlayer(Player player) {
                playerData.remove(player.getName());
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onInventoryClose(InventoryCloseEvent event) {
                if (!(event.getPlayer() instanceof Player)) return;
                Player player = (Player)event.getPlayer();
                Inventory inventory = getInventory(player);
                if (inventory == null) return;
                ItemStack[] items = inventory.getContents();
                ItemStack stack = null;
                for (ItemStack slot : items) {
                        if (slot != null) stack = slot;
                }
                if (stack == null) return;
                int amount = 0;
                PlayerMerchant merchant = PlayerMerchant.getByPlayer(player);
                for(ItemStack slot : items) {
                        if (slot == null) continue;
                        if (RealItem.canMerge(slot, stack)) {
                                amount += slot.getAmount();
                        } else {
                                for (ItemStack drop : items) if (drop != null) player.getWorld().dropItem(player.getLocation(), drop);
                                deletePlayer(player);
                                plugin.warn(player, plugin.getMessage("auction.gui.ItemsNotEqual"));
                                return;
                        }
                }
                stack = stack.clone();
                stack.setAmount(amount);
                Item item = null;
                try {
                    item = new RealItem(stack);
                } catch (IllegalArgumentException iae) {
                    for (ItemStack drop : items) if (drop != null) player.getWorld().dropItem(player.getLocation(), drop);
                    deletePlayer(player);
                    plugin.warn(player, plugin.getMessage("auction.gui.ItemsNotEqual"));
                    return;
                }
                MoneyAmount minbid = getMinbid(player);
                Auction auction = plugin.getAuctionHouse().createAuction(merchant, item, minbid, false);
                if (auction == null) {
                        for (ItemStack drop : items) if (drop != null) player.getWorld().dropItem(player.getLocation(), drop);
                } else {
                        PlayerMerchant.getByPlayer(player).msg(plugin.getMessage("auction.gui.Success").set(auction, merchant));
                }
                deletePlayer(player);
        }
}

class PlayerData {
        public MoneyAmount minbid;
        public Inventory inventory;

        public PlayerData(MoneyAmount minbid, Inventory inventory) {
                this.minbid = minbid;
                this.inventory = inventory;
        }

        public PlayerData(Inventory inventory) {
                this(new MoneyAmount(0.0), inventory);
        }

        public PlayerData(MoneyAmount minbid) {
                this(minbid, null);
        }

        public PlayerData() {
                this(new MoneyAmount(0.0), null);
        }
}
