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
import java.util.Iterator;
import java.util.Map;
import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * Represent an actual item, in fact a bukkit ItemStack.
 */
public class RealItem implements Item {
        protected ItemStack stack;
        private static final String romans[] = {"", "I", "II", "III", "IV", "V"};

        public RealItem(ItemStack stack) {
                this.stack = stack.clone();
                if (stack.getType() == Material.AIR) throw new IllegalArgumentException();
        }

        @Override
        public Item clone() {
                return new RealItem(stack.clone());
        }

        public void setAmount(int amount) {
                stack.setAmount(amount);
        }

        @Override
        public String getName() {
                try {
                        return Items.itemByStack(stack).getName();
                } catch (NullPointerException npe) {
                        return stack.getType().name(); // if vault is outdated
                }
        }

        @Override
        public String getDescription() {
                StringBuilder sb = new StringBuilder();
                if (!stack.getEnchantments().isEmpty()) {
                        sb.append(CraftBayPlugin.getInstance().getMessage("item.enchanted." + (stack.getAmount() == 1 ? "Singular" : "Plural")).toString());
                        sb.append(" ");
                }
                sb.append(getName());
                return sb.toString();
        }

        @Override
        public ItemAmount getAmount() {
                return new ItemAmount(stack.getAmount(), stack.getMaxStackSize());
        }

        @Override
        public String getEnchantments() {
                Map<Enchantment, Integer> enchantments = stack.getEnchantments();
                if (enchantments.isEmpty()) return "";
                boolean comma = false;
                StringBuffer sb = new StringBuffer();
                Iterator<Map.Entry<Enchantment, Integer>> iter = enchantments.entrySet().iterator();
                Map.Entry<Enchantment, Integer> enchantment = iter.next();
                sb.append(getEnchantmentName(enchantment.getKey())).append(" ").append(roman(enchantment.getValue()));
                while (iter.hasNext()) {
                        enchantment = iter.next();
                        sb.append(" ").append(getEnchantmentName(enchantment.getKey())).append(" ").append(roman(enchantment.getValue()));
                }
                return sb.toString();
        }

        @Override
        public int getId() {
                return stack.getTypeId();
        }

        @Override
        public int getDamage() {
                return stack.getDurability();
        }

        public ItemStack getItemStack() {
                return stack.clone();
        }

        @Override
        public boolean has(Merchant merchant) {
                return merchant.hasItem(stack);
        }

        @Override
        public Item take(Merchant merchant) {
                merchant.takeItem(stack);
                return this;
        }

        @Override
        public boolean give(Merchant merchant) {
                return merchant.giveItem(stack);
        }

        @Override
        public String toString() {
                String name = "";
                name += "" + stack.getTypeId() + ":" + stack.getDurability() + " " + stack.getAmount();
                Map<Enchantment, Integer> enchantments = stack.getEnchantments();
                if (!enchantments.isEmpty()) {
                        boolean comma = false;
                        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                                if (comma) {
                                        name += ":";
                                } else {
                                        name += " ";
                                        comma = true;
                                }
                                Enchantment enchantment = entry.getKey();
                                int level = entry.getValue();
                                name += enchantment.getName() + ":" + roman(level);
                        }
                }
                return name;
        }

        private String getEnchantmentName(Enchantment enchantment) {
                switch(enchantment.getId()) {
                case 0: return "Protection";
                case 1: return "Fire Protection";
                case 2: return "Feather Falling";
                case 3: return "Blast Protection";
                case 4: return "Projectile Protection";
                case 5: return "Respiration";
                case 6: return "Aqua Affinity";
                case 7: return "Thorns";
                case 16: return "Sharpness";
                case 17: return "Smite";
                case 18: return "Bane of Arthropods";
                case 19: return "Knockback";
                case 20: return "Fire Aspect";
                case 21: return "Looting";
                case 48: return "Power";
                case 49: return "Punch";
                case 50: return "Flame";
                case 51: return "Infinity";
                case 32: return "Efficiency";
                case 33: return "Silk Touch";
                case 34: return "Unbreaking";
                case 35: return "Fortune";
                default: return enchantment.getName();
                }
        }

        private String roman(int i) {
                try {
                        return romans[i];
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                        return "" + i;
                }
        }

        @Override
        public Map<String, Object> serialize() {
                Map<String, Object> result = new HashMap<String, Object>();
                result.put("stack", stack);
                return result;
        }

        @SuppressWarnings("unchecked")
        public static RealItem deserialize(Map<String, Object> map) {
                return new RealItem((ItemStack)map.get("stack"));
        }
}
