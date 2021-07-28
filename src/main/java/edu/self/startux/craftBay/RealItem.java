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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

/**
 * Represent an actual item, in fact a bukkit ItemStack.
 */
public final class RealItem implements Item {
    private ItemStack stack;
    private int amount;

    private static final String[] ROMANS = {"", "I", "II", "III", "IV", "V"};

    public RealItem(final ItemStack stack) {
        this(stack, stack.getAmount());
    }

    public RealItem(final ItemStack stack, final int amount) {
        if (stack == null || stack.getType() == Material.AIR) {
            throw new IllegalArgumentException("item is empty");
        }
        this.stack = stack.clone();
        this.amount = amount;
        this.stack.setAmount(1);
    }

    @Override
    public Item clone() {
        return new RealItem(stack.clone(), amount);
    }

    @Override
    public String getName() {
        return niceEnumName(stack.getType().name());
    }

    private static String capitalName(String in) {
        return "" + Character.toUpperCase(in.charAt(0)) + in.substring(1, in.length()).toLowerCase();
    }

    static String niceEnumName(String in) {
        String[] parts = in.split("_");
        StringBuilder sb = new StringBuilder(capitalName(parts[0]));
        for (int i = 1; i < parts.length; ++i) {
            sb.append(" ").append(capitalName(parts[i]));
        }
        return sb.toString();
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        if (canBeDamaged() && stack.getDurability() > 0) {
            sb.append(CraftBayPlugin.getInstance().getMessage("item.damaged.Singular").toString());
            sb.append(" ");
        }
        if (!stack.getEnchantments().isEmpty()) {
            sb.append(CraftBayPlugin.getInstance().getMessage("item.enchanted.Singular").toString());
            sb.append(" ");
        }
        sb.append(getName());
        Map<Enchantment, Integer> enchantments = stack.getEnchantments();
        ItemMeta meta = stack.getItemMeta();
        if (enchantments == null || enchantments.isEmpty()) {
            if (meta instanceof EnchantmentStorageMeta) {
                enchantments = ((EnchantmentStorageMeta) meta).getStoredEnchants();
            }
        }
        if (enchantments != null && !enchantments.isEmpty()) {
            sb.append(" (");
            int i = 0;
            for (Enchantment enchantment : enchantments.keySet()) {
                if (i++ > 0) sb.append(", ");
                sb.append(getEnchantmentName(enchantment));
                sb.append(" ");
                sb.append(roman(enchantments.get(enchantment)));
            }
            sb.append(")");
        }
        if (meta instanceof SkullMeta) {
            SkullMeta skull = (SkullMeta) meta;
            if (skull.hasOwner()) {
                sb.append(" <").append(skull.getOwner()).append(">");
            }
        }
        if (meta instanceof PotionMeta) {
            PotionMeta potions = (PotionMeta) meta;
            try {
                PotionData data = potions.getBasePotionData();
                if (data != null && data.getType() != PotionType.UNCRAFTABLE) {
                    sb.append(" ");
                    sb.append(niceEnumName(data.getType().name()));
                    if (data.isExtended()) sb.append(" Ext");
                    if (data.isUpgraded()) sb.append(" II");
                }
            } catch (IllegalArgumentException iae) { }
            if (potions.hasCustomEffects()) {
                for (PotionEffect effect: potions.getCustomEffects()) {
                    sb.append(" ");
                    sb.append(niceEnumName(effect.getType().getName()));
                    int amp = effect.getAmplifier();
                    if (amp > 0) {
                        sb.append(" ").append((amp + 1));
                    }
                }
            }
        }
        return sb.toString();
    }

    @Override
    public ItemAmount getAmount() {
        return new ItemAmount(amount, stack.getMaxStackSize());
    }

    @Override
    public String getItemInfo() {
        StringBuffer result = new StringBuffer();
        if (canBeDamaged() && stack.getDurability() > 0) {
            int durability = stack.getType().getMaxDurability() - stack.getDurability();
            durability = durability * 100 / stack.getType().getMaxDurability();
            if (durability < 0) durability = 0;
            if (durability > 100) durability = 100;
            result.append(durability).append("%");
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta.hasDisplayName()) {
            if (result.length() > 0) result.append(" ");
            result.append("\"").append(ChatColor.stripColor(meta.getDisplayName())).append("\"");
        }
        if (meta instanceof BookMeta) {
            BookMeta book = (BookMeta) meta;
            if (result.length() > 0) result.append(" ");
            if (book.hasTitle()) {
                result.append("'").append(book.getTitle()).append("'");
            }
            if (book.hasAuthor()) {
                result.append(CraftBayPlugin.getInstance().getMessage("item.book.ByAuthor")).append(book.getAuthor());
            }
            int pageCount = book.getPageCount();
            result.append(" (").append(pageCount).append(" ")
                .append(CraftBayPlugin.getInstance().getMessage(pageCount == 1 ? "item.page.Singular" : "item.page.Plural"))
                .append(")");
        }
        if (meta instanceof FireworkMeta) {
            FireworkMeta firework = (FireworkMeta) meta;
            for (FireworkEffect effect : firework.getEffects()) {
                if (result.length() > 0) result.append(" ");
                result.append(niceEnumName(effect.getType().name()));
            }
            if (result.length() > 0) result.append(" ");
            result.append(roman(firework.getPower()));
        }
        if (meta instanceof FireworkEffectMeta) {
            FireworkEffectMeta effect = (FireworkEffectMeta) meta;
            if (result.length() > 0) result.append(" ");
            result.append(niceEnumName(effect.getEffect().getType().name()));
        }
        if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta armor = (LeatherArmorMeta) meta;
            Color color = armor.getColor();
            if (result.length() > 0) result.append(" ");
            result.append(color.getRed()).append("r,").append(color.getGreen()).append("g,").append(color.getBlue()).append("b");
        }
        if (meta instanceof SkullMeta) {
            SkullMeta skull = (SkullMeta) meta;
            if (skull.hasOwner()) {
                if (result.length() > 0) result.append(" ");
                result.append("<").append(skull.getOwner()).append(">");
            }
        }
        do {
            Map<Enchantment, Integer> enchantments = meta.getEnchants();
            Iterator<Map.Entry<Enchantment, Integer>> iter = enchantments.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Enchantment, Integer> enchantment = iter.next();
                if (result.length() > 0) result.append(" ");
                result.append(getEnchantmentName(enchantment.getKey())).append(" ").append(roman(enchantment.getValue()));
            }
        } while (false);
        if (meta instanceof EnchantmentStorageMeta) {
            Map<Enchantment, Integer> enchantments = ((EnchantmentStorageMeta) meta).getStoredEnchants();
            Iterator<Map.Entry<Enchantment, Integer>> iter = enchantments.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Enchantment, Integer> enchantment = iter.next();
                if (result.length() > 0) result.append(" ");
                result.append(getEnchantmentName(enchantment.getKey())).append(" ").append(roman(enchantment.getValue()));
            }
        }
        if (meta instanceof PotionMeta) {
            PotionMeta potions = (PotionMeta) meta;
            try {
                PotionData data = potions.getBasePotionData();
                if (data != null) {
                    if (result.length() > 0) result.append(" ");
                    result.append(niceEnumName(data.getType().name()));
                    if (data.isExtended()) result.append(" Ext");
                    if (data.isUpgraded()) result.append(" II");
                }
            } catch (IllegalArgumentException iae) { }
            if (potions.hasCustomEffects()) {
                for (PotionEffect effect: potions.getCustomEffects()) {
                    if (result.length() > 0) result.append(" ");
                    result.append(niceEnumName(effect.getType().getName()));
                    int amp = effect.getAmplifier();
                    if (amp > 0) {
                        result.append(" ").append((amp + 1));
                    }
                }
            }
        }
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore.size() > 0) {
                if (result.length() > 0) result.append(" ");
                result.append("\"").append(ChatColor.stripColor(lore.get(0))).append("\"");
            }
            for (int i = 1; i < lore.size(); ++i) {
                result.append(" - ").append("\"").append(ChatColor.stripColor(lore.get(i))).append("\"");
            }
        }
        return result.toString();
    }

    public ItemStack getItemStack() {
        return stack.clone();
    }

    @Override
    public boolean has(Merchant merchant) {
        return merchant.hasItem(stack);
    }

    @Override
    public boolean give(Merchant merchant) {
        if (merchant instanceof PlayerMerchant) {
            PlayerMerchant playerMerchant = (PlayerMerchant) merchant;
            Player player = playerMerchant.getPlayer();
            if (player == null) return false;
            int due = amount;
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
        } else {
            return true;
        }
    }

    @Override
    public String toString() {
        String name = "";
        name += "" + stack.getType() + "x" + amount;
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
        return a.isSimilar(b);
    }

    private boolean canBeDamaged() {
        return stack.getType().getMaxDurability() > 0;
    }

    private boolean between(int pivot, int lower, int upper) {
        return pivot >= lower && pivot <= upper;
    }

    public static String getEnchantmentName(Enchantment enchantment) {
        return niceEnumName(enchantment.getKey().getKey());
    }

    private String roman(int i) {
        try {
            return ROMANS[i];
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            return "" + i;
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("stack", stack);
        result.put("amount", amount);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static RealItem deserialize(Map<String, Object> map) {
        ItemStack stack = (ItemStack) map.get("stack");
        Object ao = map.get("amount");
        if (ao == null) {
            return new RealItem(stack);
        } else {
            int amount = (Integer) ao;
            return new RealItem(stack, amount);
        }
    }

    @Override
    public Component toComponent() {
        Component itemName;
        if (stack.hasItemMeta()) {
            ItemMeta itemMeta = stack.getItemMeta();
            itemName = itemMeta.hasDisplayName()
                ? itemMeta.displayName()
                : Component.text(stack.getI18NDisplayName());
        } else {
            itemName = Component.text(stack.getI18NDisplayName());
        }
        return itemName.hoverEvent(stack.asHoverEvent())
            .clickEvent(ClickEvent.runCommand("/auc preview"));
    }
}
