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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.InputStream;

public class Locale {
        private CraftBayPlugin plugin;
        private Map<String, Message> map = new HashMap<String, Message>();

        public Locale(CraftBayPlugin plugin, String locale) {
                this.plugin = plugin;
                loadLocaleFile("en_US");
                if (!locale.equals("en_US")) loadLocaleFile(locale);
        }

        public Locale(CraftBayPlugin plugin) {
                this(plugin, "en_US");
        }

        @SuppressWarnings("unchecked")
        private void loadLocaleFile(String name) {
                String path = "lang/" + name + ".yml";
                InputStream inp = plugin.getResource(path);
                if (inp == null) {
                        plugin.log("Language file not found: \"" + name + "\"", Level.WARNING);
                        return;
                }
                ConfigurationSection conf = YamlConfiguration.loadConfiguration(inp);
                for (String key : conf.getKeys(true)) {
                        Message message;
                        Object o = conf.get(key);
                        if (o instanceof List) {
                                message = new Message((List<String>)o);
                        } else if (o instanceof String) {
                                message = new Message((String)o);
                        } else {
                                continue;
                        }
                        map.put(key.toLowerCase(), message);
                        // System.out.println(key);
                        // System.out.println(message.toString());
                }
        }

        public Message getMessage(String key) {
                if (key == null) return new Message();
                Message result = map.get(key.toLowerCase());
                if (result == null) {
                        System.err.println("Locale key not found: " + key);
                        return new Message();
                }
                return result.clone();
        }

        public Message getMessages(String... keys) {
                Message result = new Message();
                for (String key : keys) {
                        result.append(getMessage(key));
                }
                return result;
        }
}