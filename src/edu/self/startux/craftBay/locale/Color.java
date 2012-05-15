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

import org.bukkit.ChatColor;
import java.util.HashMap;
import java.util.Map;

public class Color {
        public static Color DEFAULT = new Color(new String[]{ "FOO", "DEFAULT", "DFL" }, ChatColor.BLUE);
        public static Color HEADER = new Color(new String[]{ "HEADER", "H" }, ChatColor.YELLOW);
        public static Color HIGHLIGHT = new Color(new String[]{ "HIGHLIGHT", "HL", "HI" }, ChatColor.AQUA);
        public static Color SHADOW = new Color(new String[]{ "SHADOW", "DARK", "SHADE", "SHD" }, ChatColor.DARK_GRAY);
        public static Color SHOTCUT = new Color(new String[]{ "SHORTCUT", "S" }, ChatColor.WHITE);
        public static Color ADMIN = new Color(new String[]{ "ADMIN", "ADM" }, ChatColor.DARK_RED);
        public static Color ADMINHIGHLIGHT = new Color(new String[]{ "ADMINHIGHLIGHT", "ADMINHI", "ADMHL", "ADMHI" }, ChatColor.RED);
        public static Color ERROR = new Color(new String[]{ "ERROR", "ERR", "WARN" }, ChatColor.RED);

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
}