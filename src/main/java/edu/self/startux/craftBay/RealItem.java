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

import edu.self.startux.craftBay.item.ItemManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
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
    public Component getName() {
        return getDisplayName(stack);
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
    public Component getDescription() {
        TextComponent.Builder result = Component.text();
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof Damageable damageable) {
            if (damageable.hasDamage()) {
                result.append(Component.text(CraftBayPlugin.getInstance().getMessage("item.damaged.Singular").toString() + " "));
            }
        }
        if (!stack.getEnchantments().isEmpty()) {
            result.append(Component.text(CraftBayPlugin.getInstance().getMessage("item.enchanted.Singular").toString() + " "));
        }
        result.append(getDisplayName(stack));
        Map<Enchantment, Integer> enchantments = stack.getEnchantments();
        if (enchantments == null || enchantments.isEmpty()) {
            if (meta instanceof EnchantmentStorageMeta) {
                enchantments = ((EnchantmentStorageMeta) meta).getStoredEnchants();
            }
        }
        if (enchantments != null && !enchantments.isEmpty()) {
            StringBuilder sb = new StringBuilder(" (");
            int i = 0;
            for (Enchantment enchantment : enchantments.keySet()) {
                if (i++ > 0) sb.append(", ");
                sb.append(getEnchantmentName(enchantment));
                sb.append(" ");
                sb.append(roman(enchantments.get(enchantment)));
            }
            sb.append(")");
            result.append(Component.text(sb.toString()));
        }
        if (meta instanceof SkullMeta) {
            SkullMeta skull = (SkullMeta) meta;
            if (skull.hasOwner()) {
                OfflinePlayer owner = skull.getOwningPlayer();
                if (owner != null) {
                    result.append(Component.text(" <" + owner.getName() + ">"));
                }
            }
        }
        if (meta instanceof PotionMeta) {
            PotionMeta potions = (PotionMeta) meta;
            StringBuilder sb = new StringBuilder();
            try {
                PotionData data = potions.getBasePotionData();
                if (data != null && data.getType() != PotionType.AWKWARD) {
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
            result.append(Component.text(sb.toString()));
        }
        return result.build();
    }

    @Override
    public ItemAmount getAmount() {
        return new ItemAmount(amount, stack.getMaxStackSize());
    }

    @Override
    public Component getItemInfo() {
        for (ItemManager itemManager : CraftBayPlugin.getInstance().getItemManagers()) {
            if (!itemManager.isManaged(stack)) continue;
            return itemManager.getItemInfo(stack);
        }
        StringBuffer result = new StringBuffer();
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof Damageable damageable) {
            int maxDurability = stack.getType().getMaxDurability();
            if (maxDurability > 0) {
                int durability = (int) stack.getType().getMaxDurability() - damageable.getDamage();
                durability = (durability * 100) / maxDurability;
                if (durability < 0) durability = 0;
                if (durability > 100) durability = 100;
                result.append(durability).append("%");
            }
        }
        if (meta.hasDisplayName()) {
            if (result.length() > 0) result.append(" ");
            Component displayName = meta.displayName();
            if (displayName != null) {
                String plain = PlainTextComponentSerializer.plainText().serialize(displayName);
                result.append("\"").append(plain).append("\"");
            }
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
                OfflinePlayer owner = skull.getOwningPlayer();
                if (owner != null) {
                    if (result.length() > 0) result.append(" ");
                    result.append("<").append(owner.getName()).append(">");
                }
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
        return Component.text(result.toString());
    }

    public ItemStack getItemStack() {
        return stack.clone();
    }

    @Override
    public boolean has(Merchant merchant) {
        return merchant.hasItem(stack);
    }

    @Override
    public List<ItemStack> toItemStackList() {
        List<ItemStack> result = new ArrayList<>();
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
            result.add(other);
        }
        return result;
    }

    private static String enumToCamelCase(String in) {
        return Stream.of(in.split("_"))
            .map(w -> w.substring(0, 1).toUpperCase() + w.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
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
                name += enumToCamelCase(enchantment.getKey().getKey()) + ":" + roman(level);
            }
        }
        return name;
    }

    public static boolean canMerge(ItemStack a, ItemStack b) {
        return a.isSimilar(b);
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
        return getDisplayName(stack)
            .hoverEvent(stack.asHoverEvent())
            .clickEvent(ClickEvent.runCommand("/auc preview"));
    }

    public static Component getDisplayName(ItemStack itemStack) {
        for (ItemManager itemManager : CraftBayPlugin.getInstance().getItemManagers()) {
            if (!itemManager.isManaged(itemStack)) continue;
            return itemManager.getDisplayName(itemStack);
        }
        if (CraftBayPlugin.getInstance().isShowCustomItemNames()) {
            if (itemStack.hasItemMeta()) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta.hasDisplayName()) {
                    Component displayName = meta.displayName();
                    if (!Component.empty().equals(displayName)) {
                        return displayName;
                    }
                }
            }
        }
        return Component.text(itemStack.getI18NDisplayName());
    }
}
