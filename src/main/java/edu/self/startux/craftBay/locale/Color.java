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

import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import edu.self.startux.craftBay.CraftBayPlugin;

public class Color {
        public static Color DEFAULT = new Color(new String[]{ "DEFAULT", "DFL" }, ChatColor.BLUE);
        public static Color HEADER = new Color(new String[]{ "HEADER", "HEAD", "HD", "H" }, ChatColor.YELLOW);
        public static Color HIGHLIGHT = new Color(new String[]{ "HIGHLIGHT", "HL", "HI" }, ChatColor.AQUA);
        public static Color SHADOW = new Color(new String[]{ "SHADOW", "DARK", "SHADE", "SHD" }, ChatColor.DARK_GRAY);
        public static Color SHORTCUT = new Color(new String[]{ "SHORTCUT", "SC", "S" }, ChatColor.WHITE);
        public static Color ADMIN = new Color(new String[]{ "ADMIN", "ADM" }, ChatColor.DARK_RED);
        public static Color ADMINHIGHLIGHT = new Color(new String[]{ "ADMINHIGHLIGHT", "ADMINHIGH", "ADMINHI", "ADMHL", "ADMHI" }, ChatColor.RED);
        public static Color ERROR = new Color(new String[]{ "ERROR", "ERR" }, ChatColor.DARK_RED);
        public static Color WARN = new Color(new String[]{ "WARNING", "WARN", "WRN" }, ChatColor.RED);
        public static Color WARNHIGHLIGHT = new Color(new String[]{ "WARNINGHIGHLIGHT", "WARNINGHIGH", "WARNHIGH", "WARNHI", "WRNHI" }, ChatColor.DARK_RED);

        private static Map<String, Color> nameMap;
        private ChatColor chatColor;

        private Color(String[] aliases, ChatColor dfl) {
                if (nameMap == null) nameMap = new HashMap<String, Color>();
                for (String alias : aliases) nameMap.put(alias.toLowerCase(), this);
                this.chatColor = dfl;
        }

        public static Color getByName(String name) {
                if (name == null) return null;
                return nameMap.get(name.toLowerCase());
        }

        public void setColor(ChatColor color) {
                this.chatColor = color;
        }
        
        @Override
        public String toString() {
                return chatColor.toString();
        }

        public static ChatColor getChatColorByName(String name) {
                for (ChatColor col : ChatColor.values()) if (col.name().equalsIgnoreCase(name)) return col;
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
