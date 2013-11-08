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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerMerchant implements Merchant {
        private String playerName;

        public PlayerMerchant(String name) {
                playerName = name;
        }

        private PlayerMerchant(OfflinePlayer player) {
                playerName = player.getName();
        }

        public static PlayerMerchant getByPlayer(OfflinePlayer player) {
                return new PlayerMerchant(player);
        }

        public Player getPlayer() {
                return Bukkit.getServer().getPlayerExact(playerName);
        }

        private static CraftBayPlugin getPlugin() {
                return CraftBayPlugin.getInstance();
        }

        @Override
        public String getName() {
                return playerName;
        }

        @Override
        public boolean hasAmount(MoneyAmount amount) {
                if (amount.getDouble() < 0.0) {
                        throw new IllegalArgumentException("given amount must be positive!");
                }
                if (!getPlugin().getEco().has(playerName, amount.getDouble())) {
                        return false;
                }
                return true;
        }

        @Override
        public boolean giveAmount(MoneyAmount amount) {
                if (amount.getDouble() < 0.0) {
                        throw new IllegalArgumentException("given amount must be positive!");
                }
                MoneyAmount before = new MoneyAmount(getPlugin().getEco().getBalance(playerName));
                boolean success = getPlugin().getEco().depositPlayer(playerName, amount.getDouble()).transactionSuccess();
                MoneyAmount after = new MoneyAmount(getPlugin().getEco().getBalance(playerName));
                if (getPlugin().getDebugMode()) {
                        getPlugin().getLogger().info(String.format("GIVE player='%s' amount='%s' success='%b' before='%s' after='%s'", playerName, amount, success, before, after));
                }
                return success;
        }

        @Override
        public boolean takeAmount(MoneyAmount amount) {
                if (amount.getDouble() < 0.0) {
                        throw new IllegalArgumentException("take amount must be positive!");
                }
                MoneyAmount before = new MoneyAmount(getPlugin().getEco().getBalance(playerName));
                boolean success = getPlugin().getEco().withdrawPlayer(playerName, amount.getDouble()).transactionSuccess();
                MoneyAmount after = new MoneyAmount(getPlugin().getEco().getBalance(playerName));
                if (getPlugin().getDebugMode()) {
                        getPlugin().getLogger().info(String.format("TAKE player='%s' amount='%s' success='%b' before='%s' after='%s'", playerName, amount, success, before, after));
                }
                return success;
        }

        @Override
        public boolean hasItem(ItemStack stack) {
                Player player = getPlayer();
                Map<Integer, ? extends ItemStack> ret = player.getInventory().all(stack.getType());
                int found = 0;
                for (ItemStack slot : ret.values()) {
                        if (RealItem.canMerge(slot, stack)) {
                                found += slot.getAmount();
                        }
                }
                if (found < stack.getAmount()) {
                        return false;
                }
                return true;
        }

        @Override
        public boolean takeItem(ItemStack stack) {
                if (stack == null) return true;
                Player player = getPlayer();
                Map<Integer, ? extends ItemStack> ret = player.getInventory().all(stack.getType());
                int needed = stack.getAmount();
                for (Map.Entry<Integer, ? extends ItemStack> entry : ret.entrySet()) {
                        Integer ix = entry.getKey();
                        ItemStack slot = entry.getValue();
                        if (RealItem.canMerge(slot, stack)) {
                                if (needed <= 0) break;
                                if (slot.getAmount() <= needed) { // catches amount=1
                                        needed -= slot.getAmount();
                                        player.getInventory().clear(ix);
                                } else {
                                        slot.setAmount(slot.getAmount() - needed);
                                        player.getInventory().setItem(ix, slot);
                                        needed = 0;
                                        break;
                                }
                        }
                }
                return true;
        }

        @Override
        public boolean giveItem(ItemStack stack) {
                if (stack == null) return true;
                Player player = getPlayer();
                if (player == null) return false;
                int due = stack.getAmount();
                int stackSize = stack.getMaxStackSize();
                if (stackSize < 1) {
                        stackSize = 1;
                }
                while (due > 0) {
                        ItemStack other = stack.clone();
                        if (due < stackSize) {
                                other.setAmount(due);
                                due = 0;
                        } else {
                                other.setAmount(stackSize);
                                due -= stackSize;
                        }
                        Map<Integer, ItemStack> ret = player.getInventory().addItem(other);
                        for (ItemStack item : ret.values()) {
                                player.getWorld().dropItem(player.getLocation(), item);
                        }
                }
                if (!player.isOnline()) {
                        player.saveData();
                }
                return true;
        }

        /**
         * Check if the Player represented by the Merchant has a
         * Bukkit permission.
         * @param permission the permission
         * @return true if he has it, false otherwise
         */
        @Override
        public boolean hasPermission(String permission) {
                Player player = getPlayer();
                if (player == null) return false;
                return player.hasPermission(permission);
        }

        @Override
        public void msg(Message msg) {
                if (getPlayer() == null) return;
                getPlugin().msg(getPlayer(), msg);
        }

        @Override
        public void warn(Message msg) {
                if (getPlayer() == null) return;
                getPlugin().warn(getPlayer(), msg);
        }

        @Override
        public boolean equals(Object o) {
                if (o == null) return false;
                if (!(o instanceof PlayerMerchant)) return false;
                PlayerMerchant other = (PlayerMerchant)o;
                if (!playerName.equalsIgnoreCase(other.playerName)) return false;
                return true;
        }

        @Override
        public Map<String, Object> serialize() {
                Map<String, Object> result = new HashMap<String, Object>();
                result.put("player", playerName);
                return result;
        }

        @SuppressWarnings("unchecked")
        public static PlayerMerchant deserialize(Map<String, Object> map) {
                Object o = map.get("player");
                // support legacy serialization using OfflinePlayer
                if (o instanceof OfflinePlayer) {
                        return new PlayerMerchant((OfflinePlayer)o);
                } else if (o instanceof String) {
                        return new PlayerMerchant((String)o);
                } else {
                        return null;
                }
        }

        @Override
        public boolean isListening() {
                Player player = getPlayer();
                if (player == null) return false;
                return getPlugin().getChatPlugin().isListening(player);
        }

        @Override
        public Merchant clone() {
                return new PlayerMerchant(playerName);
        }
}
