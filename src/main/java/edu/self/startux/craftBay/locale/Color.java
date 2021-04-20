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

package edu.self.startux.craftBay.locale;

import edu.self.startux.craftBay.CraftBayPlugin;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public final class Color {
    public static final Color DEFAULT = new Color(new String[]{"DEFAULT", "DFL"}, ChatColor.BLUE);
    public static final Color HEADER = new Color(new String[]{"HEADER", "HEAD", "HD", "H"}, ChatColor.YELLOW);
    public static final Color HIGHLIGHT = new Color(new String[]{"HIGHLIGHT", "HL", "HI"}, ChatColor.AQUA);
    public static final Color SHADOW = new Color(new String[]{"SHADOW", "DARK", "SHADE", "SHD"}, ChatColor.DARK_GRAY);
    public static final Color SHORTCUT = new Color(new String[]{"SHORTCUT", "SC", "S"}, ChatColor.WHITE);
    public static final Color ADMIN = new Color(new String[]{"ADMIN", "ADM"}, ChatColor.DARK_RED);
    public static final Color ADMINHIGHLIGHT = new Color(new String[]{"ADMINHIGHLIGHT", "ADMINHIGH", "ADMINHI", "ADMHL", "ADMHI"}, ChatColor.RED);
    public static final Color ERROR = new Color(new String[]{"ERROR", "ERR"}, ChatColor.DARK_RED);
    public static final Color WARN = new Color(new String[]{"WARNING", "WARN", "WRN"}, ChatColor.RED);
    public static final Color WARNHIGHLIGHT = new Color(new String[]{"WARNINGHIGHLIGHT", "WARNINGHIGH", "WARNHIGH", "WARNHI", "WRNHI"}, ChatColor.DARK_RED);

    private static Map<String, Color> nameMap;
    private ChatColor chatColor;
    private TextColor textColor;

    private Color(final String[] aliases, final ChatColor dfl) {
        if (nameMap == null) nameMap = new HashMap<String, Color>();
        for (String alias : aliases) nameMap.put(alias.toLowerCase(), this);
        this.chatColor = dfl;
        this.textColor = NamedTextColor.NAMES.value(dfl.name().toLowerCase());
    }

    public static Color getByName(String name) {
        if (name == null) return null;
        return nameMap.get(name.toLowerCase());
    }

    public void setColor(ChatColor color) {
        this.chatColor = color;
        this.textColor = NamedTextColor.NAMES.value(color.name().toLowerCase());
    }

    @Override
    public String toString() {
        return chatColor.toString();
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public TextColor getTextColor() {
        return textColor;
    }

    public static ChatColor getChatColorByName(String name) {
        for (ChatColor col : ChatColor.values()) {
            if (col.name().equalsIgnoreCase(name)) {
                return col;
            }
        }
        throw new IllegalArgumentException("No such ChatColor: " + name);
    }

    public static void configure(ConfigurationSection section) {
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            Color color = getByName(key);
            if (color == null) {
                CraftBayPlugin.getInstance().getLogger().warning("Unknown color key: " + key);
                continue;
            }
            String value = section.getString(key);
            try {
                if (value.length() == 1) {
                    color.setColor(ChatColor.getByChar(value));
                } else {
                    color.setColor(getChatColorByName(value));
                }
            } catch (Exception e) {
                CraftBayPlugin.getInstance().getLogger().warning("Unknown color value: " + value);
                continue;
            }
        }
    }
}
