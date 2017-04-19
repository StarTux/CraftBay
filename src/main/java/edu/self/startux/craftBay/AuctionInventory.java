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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class AuctionInventory implements Listener {
    private CraftBayPlugin plugin;
    private Map<UUID, PlayerData> playerData = new HashMap<>();

    public AuctionInventory(CraftBayPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    void onDisable() {
        for (UUID uuid: playerData.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) player.closeInventory();
        }
    }

    public void setPlayer(Player player, MoneyAmount minbid, Inventory inventory, boolean preview) {
        PlayerData data = playerData.get(player.getUniqueId());
        if (data == null) {
            data = new PlayerData(minbid, inventory, preview);
            playerData.put(player.getUniqueId(), data);
        } else {
            data.minbid = minbid;
            data.inventory = inventory;
            data.preview = preview;
        }
    }

    public void initPlayer(Player player, MoneyAmount minbid) {
        String title = plugin.getMessage("auction.gui.ChestTitle").toString();
        if (title.length() > 32) title = title.substring(0, 32);
        Inventory inventory = Bukkit.createInventory(player, 54, title);
        player.openInventory(inventory);
        setPlayer(player, minbid, inventory, false);
    }

    public boolean initPreview(Player player, Auction auction) {
        if (!(auction.getItem() instanceof RealItem)) return false;
        if (playerData.get(player.getUniqueId()) != null) return false;
        RealItem real = (RealItem)auction.getItem();
        ItemStack item = real.getItemStack();
        List<ItemStack> items = new ArrayList<>();
        int amount = real.getAmount().getInt();
        while (amount > 0) {
            int stackSize = Math.min(amount, item.getType().getMaxStackSize());
            amount -= stackSize;
            ItemStack clone = item.clone();
            clone.setAmount(stackSize);
            items.add(clone);
        }
        if (items.isEmpty()) return false;
        int invSize = ((items.size() - 1) / 9 + 1) * 9;
        Inventory inventory = Bukkit.createInventory(player, invSize, "Auction Preview");
        for (ItemStack stack: items) inventory.addItem(stack);
        player.openInventory(inventory);
        setPlayer(player, new MoneyAmount(0.0), inventory, true);
        return true;
    }

    public void deletePlayer(Player player) {
        playerData.remove(player.getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player)event.getPlayer();
        PlayerData data = playerData.get(player.getUniqueId());
        if (data == null) return;
        deletePlayer(player);
        if (data.preview) return;
        Inventory inventory = data.inventory;
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
                plugin.warn(player, plugin.getMessage("auction.gui.ItemsNotEqual"));
                return;
            }
        }
        stack = stack.clone();
        Item item = null;
        try {
            item = new RealItem(stack, amount);
        } catch (IllegalArgumentException iae) {
            for (ItemStack drop : items) if (drop != null) player.getWorld().dropItem(player.getLocation(), drop);
            plugin.warn(player, plugin.getMessage("auction.gui.ItemsNotEqual"));
            return;
        }
        MoneyAmount minbid = data.minbid;
        Auction auction = plugin.getAuctionHouse().createAuction(merchant, item, minbid, false);
        if (auction == null) {
            for (ItemStack drop : items) if (drop != null) player.getWorld().dropItem(player.getLocation(), drop);
        } else {
            PlayerMerchant.getByPlayer(player).msg(plugin.getMessage("auction.gui.Success").set(auction, merchant));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player = (Player)event.getWhoClicked();
        PlayerData data = playerData.get(player.getUniqueId());
        if (data == null) return;
        if (!data.preview) return;
        event.setCancelled(true);
        new BukkitRunnable() {
            @Override public void run() {
                player.closeInventory();
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        final Player player = (Player)event.getWhoClicked();
        PlayerData data = playerData.get(player.getUniqueId());
        if (data == null) return;
        if (!data.preview) return;
        event.setCancelled(true);
        new BukkitRunnable() {
            @Override public void run() {
                player.closeInventory();
            }
        }.runTask(plugin);
    }

    private static class PlayerData {
        public MoneyAmount minbid;
        public Inventory inventory;
        public boolean preview;

        public PlayerData(MoneyAmount minbid, Inventory inventory, boolean preview) {
            this.minbid = minbid;
            this.inventory = inventory;
            this.preview = preview;
        }
    }
}
