/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright 2012-2021 StarTux
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

import edu.self.startux.craftBay.locale.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public final class AuctionInventory implements Listener {
    private CraftBayPlugin plugin;
    private Map<UUID, PlayerData> playerData = new HashMap<>();

    public AuctionInventory(final CraftBayPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    void onDisable() {
        for (UUID uuid: playerData.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) player.closeInventory();
        }
    }

    public void setPlayer(Player player, MoneyAmount minbid, Inventory inventory, Type type) {
        PlayerData data = playerData.get(player.getUniqueId());
        if (data == null) {
            data = new PlayerData(minbid, inventory, type);
            playerData.put(player.getUniqueId(), data);
        } else {
            data.minbid = minbid;
            data.inventory = inventory;
            data.type = type;
        }
    }

    public void initPlayer(Player player, MoneyAmount minbid) {
        String title = plugin.getMessage("auction.gui.ChestTitle").toString();
        if (title.length() > 32) title = title.substring(0, 32);
        Inventory inventory = Bukkit.createInventory(null, 54, Component.text(title));
        player.openInventory(inventory);
        setPlayer(player, minbid, inventory, Type.CREATE);
    }

    public boolean initPreview(Player player, Auction auction) {
        if (!(auction.getItem() instanceof RealItem)) return false;
        if (playerData.get(player.getUniqueId()) != null) return false;
        RealItem real = (RealItem) auction.getItem();
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
        Component name = Component.text("Auction Preview", Color.DEFAULT.getTextColor());
        Inventory inventory = Bukkit.createInventory(null, invSize, name);
        for (ItemStack stack: items) inventory.addItem(stack);
        player.openInventory(inventory);
        setPlayer(player, new MoneyAmount(0.0), inventory, Type.PREVIEW);
        return true;
    }

    public void initDelivery(Player player, List<ItemStack> items) {
        int size = 9 * Math.min(6, ((items.size() - 1) / 9 + 1));
        Inventory inventory = Bukkit.createInventory(null, size, Component.text("Item Delivery"));
        for (ItemStack item : items) {
            inventory.addItem(item);
        }
        player.openInventory(inventory);
        setPlayer(player, MoneyAmount.ZERO, inventory, Type.DELIVERY);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    public boolean initPreview(Player player, Inventory original) {
        Component name = Component.text("Auction Preview", Color.DEFAULT.getTextColor());
        Inventory inventory = Bukkit.createInventory(null, original.getSize(), name);
        for (int i = 0; i < original.getSize(); i += 1) {
            ItemStack itemStack = original.getItem(i);
            if (itemStack == null) continue;
            inventory.setItem(i, itemStack.clone());
        }
        player.openInventory(inventory);
        setPlayer(player, new MoneyAmount(0.0), inventory, Type.PREVIEW);
        return true;
    }

    public void deletePlayer(Player player) {
        playerData.remove(player.getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        PlayerData data = playerData.get(player.getUniqueId());
        if (data == null) return;
        deletePlayer(player);
        if (data.type == Type.CREATE) {
            Inventory inventory = data.inventory;
            if (inventory == null) return;
            List<ItemStack> items = new ArrayList<>();
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    items.add(item);
                }
            }
            if (items.isEmpty()) return;
            ItemStack stack = items.get(0).clone();
            int amount = 0;
            PlayerMerchant merchant = PlayerMerchant.getByPlayer(player);
            for (ItemStack item : items) {
                if (RealItem.canMerge(item, stack)) {
                    amount += item.getAmount();
                } else {
                    for (ItemStack drop : player.getInventory().addItem(items.toArray(new ItemStack[0])).values()) {
                        player.getWorld().dropItem(player.getEyeLocation(), drop);
                    }
                    plugin.warn(player, plugin.getMessage("auction.gui.ItemsNotEqual"));
                    return;
                }
            }
            Item item = null;
            try {
                item = new RealItem(stack, amount);
            } catch (IllegalArgumentException iae) {
                for (ItemStack drop : player.getInventory().addItem(items.toArray(new ItemStack[0])).values()) {
                    player.getWorld().dropItem(player.getEyeLocation(), drop);
                }
                plugin.warn(player, plugin.getMessage("auction.gui.ItemsNotEqual"));
                return;
            }
            MoneyAmount minbid = data.minbid;
            Auction auction = plugin.getAuctionHouse().createAuction(merchant, item, minbid);
            if (auction == null) {
                for (ItemStack drop : player.getInventory().addItem(items.toArray(new ItemStack[0])).values()) {
                    player.getWorld().dropItem(player.getEyeLocation(), drop);
                }
            } else {
                PlayerMerchant.getByPlayer(player).msg(plugin.getMessage("auction.gui.Success").set(auction, merchant));
            }
            return;
        }
        if (data.type == Type.PREVIEW) return;
        if (data.type == Type.DELIVERY) {
            for (ItemStack item : data.inventory) {
                if (item == null || item.getType() == Material.AIR) continue;
                for (ItemStack drop : player.getInventory().addItem(item).values()) {
                    player.getWorld().dropItem(player.getLocation(), drop);
                }
            }
            Bukkit.getScheduler().runTaskLater(CraftBayPlugin.getInstance(), () -> {
                    CraftBayPlugin.getInstance().getAuctionScheduler().checkDeliveries();
                }, 20L);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.25f, 2.0f);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        PlayerData data = playerData.get(player.getUniqueId());
        if (data == null) return;
        if (data.type == Type.CREATE) return;
        if (data.type == Type.PREVIEW) {
            event.setCancelled(true);
            ItemStack itemStack = event.getCurrentItem();
            if (itemStack != null && Tag.SHULKER_BOXES.isTagged(itemStack.getType())) {
                BlockStateMeta meta = (BlockStateMeta) itemStack.getItemMeta();
                ShulkerBox box = (ShulkerBox) meta.getBlockState();
                Inventory boxInventory = box.getSnapshotInventory();
                Bukkit.getScheduler().runTask(plugin, () -> {
                        deletePlayer(player);
                        initPreview(player, boxInventory);
                    });
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                });
            return;
        }
        if (data.type == Type.DELIVERY) return;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        final Player player = (Player) event.getWhoClicked();
        PlayerData data = playerData.get(player.getUniqueId());
        if (data == null) return;
        if (data.type == Type.CREATE) return;
        if (data.type == Type.PREVIEW) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, () -> {
                    player.closeInventory();
                });
            return;
        }
        if (data.type == Type.DELIVERY) return;
    }

    public enum Type {
        CREATE,
        PREVIEW,
        DELIVERY;
    }

    private static class PlayerData {
        private MoneyAmount minbid;
        private Inventory inventory;
        private Type type;

        PlayerData(final MoneyAmount minbid, final Inventory inventory, final Type type) {
            this.minbid = minbid;
            this.inventory = inventory;
            this.type = type;
        }
    }
}
