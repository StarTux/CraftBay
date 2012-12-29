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
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

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
                        // if vault is outdated
                        String[] parts = stack.getType().name().split("_");
                        StringBuilder sb = new StringBuilder(capitalName(parts[0]));
                        for (int i = 1; i < parts.length; ++i) {
                                sb.append(" ").append(capitalName(parts[i]));
                        }
                        return sb.toString();
                }
        }

        private static String capitalName(String in) {
                return "" + Character.toUpperCase(in.charAt(0)) + in.substring(1, in.length()).toLowerCase();
        }

        @Override
        public String getDescription() {
                StringBuilder sb = new StringBuilder();
                if (!stack.getType().isBlock() && stack.getDurability() > 0) {
                        sb.append(CraftBayPlugin.getInstance().getMessage("item.damaged.Singular").toString());
                        sb.append(" ");
                }
                if (!stack.getEnchantments().isEmpty()) {
                        sb.append(CraftBayPlugin.getInstance().getMessage("item.enchanted.Singular").toString());
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
                StringBuffer result = new StringBuffer();
                ItemMeta meta = stack.getItemMeta();
                if (meta instanceof BookMeta) {
                        BookMeta book = (BookMeta)meta;
                        if (result.length() > 0) result.append(" ");
                        result.append(book.hasTitle() ? book.getTitle() : "notitle").append(" by ").append(book.hasAuthor() ? book.getAuthor() : "noname");
                }
                if (meta instanceof FireworkMeta) {
                        FireworkMeta firework = (FireworkMeta)meta;
                        for (FireworkEffect effect : firework.getEffects()) {
                                if (result.length() > 0) result.append(" ");
                                result.append(capitalName(effect.getType().name()));
                        }
                        if (result.length() > 0) result.append(" ");
                        result.append(roman(firework.getPower()));
                }
                if (meta instanceof FireworkEffectMeta) {
                        FireworkEffectMeta effect = (FireworkEffectMeta)meta;
                        if (result.length() > 0) result.append(" ");
                        result.append(capitalName(effect.getEffect().getType().name()));
                }
                if (meta instanceof LeatherArmorMeta) {
                        LeatherArmorMeta armor = (LeatherArmorMeta)meta;
                        Color color = armor.getColor();
                        if (result.length() > 0) result.append(" ");
                        result.append(color.getRed()).append("r,").append(color.getGreen()).append("g,").append(color.getBlue()).append("b");
                }
                {
                        Map<Enchantment, Integer> enchantments = meta.getEnchants();
                        Iterator<Map.Entry<Enchantment, Integer>> iter = enchantments.entrySet().iterator();
                        while (iter.hasNext()) {
                                Map.Entry<Enchantment, Integer> enchantment = iter.next();
                                if (result.length() > 0) result.append(" ");
                                result.append(getEnchantmentName(enchantment.getKey())).append(" ").append(roman(enchantment.getValue()));
                        }
                }
                if (meta instanceof EnchantmentStorageMeta) {
                        Map<Enchantment, Integer> enchantments = ((EnchantmentStorageMeta)meta).getStoredEnchants();
                        Iterator<Map.Entry<Enchantment, Integer>> iter = enchantments.entrySet().iterator();
                        while (iter.hasNext()) {
                                Map.Entry<Enchantment, Integer> enchantment = iter.next();
                                if (result.length() > 0) result.append(" ");
                                result.append(getEnchantmentName(enchantment.getKey())).append(" ").append(roman(enchantment.getValue()));
                        }
                }
                return result.toString();
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

        public static boolean canMerge(ItemStack a, ItemStack b) {
                return a.getType() == b.getType() && a.getDurability() == b.getDurability() && a.getItemMeta().equals(b.getItemMeta());
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
