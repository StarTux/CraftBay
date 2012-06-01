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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class Locale {
        private CraftBayPlugin plugin;
        private Map<String, Message> map = new HashMap<String, Message>();

        public Locale(CraftBayPlugin plugin, String locale) {
                this.plugin = plugin;
                writeLocaleFiles();
                loadLocaleResource("en_US");
                loadLocaleFile(locale);
        }

        public Locale(CraftBayPlugin plugin) {
                this(plugin, "en_US");
        }

        private void writeLocaleFiles() {
                BufferedReader reader = new BufferedReader(new InputStreamReader(plugin.getResource("lang/list")));
                while (true) {
                        String line = null;
                        try {
                                line = reader.readLine();
                        } catch (IOException ioe) {
                                ioe.getCause().printStackTrace();
                                return;
                        }
                        if (line == null) break;
                        InputStream inp = plugin.getResource("lang/" + line);
                        if (inp == null) {
                                plugin.log("Unable to locate resource: \"lang/" + line + "\"", Level.SEVERE);
                                continue;
                        }
                        File file = plugin.getDataFolder();
                        if (!file.exists()) file.mkdir();
                        file = new File(file, "lang");
                        if (!file.exists()) file.mkdir();
                        file = new File(file, line);
                        // if (file.exists()) continue;
                        try {
                                FileOutputStream out = new FileOutputStream(file);
                                while (true) {
                                        int b = inp.read();
                                        if (b == -1) break;
                                        out.write(b);
                                }
                                out.close();
                        } catch (IOException ioe) {
                                plugin.log("Error while trying to write \"" + file.getPath() + "\"");
                                continue;
                        }
                        try {
                                inp.close();
                        } catch (IOException ioe) {
                                ioe.getCause().printStackTrace();
                        }
                }
        }

        private void loadLocaleResource(String name) {
                String path = "lang/" + name + ".yml";
                InputStream inp = plugin.getResource(path);
                if (inp == null) {
                        plugin.log("Language file not found: \"" + name + "\"", Level.WARNING);
                        return;
                }
                loadLocaleStream(inp);
        }

        private void loadLocaleFile(String name) {
                File file = plugin.getDataFolder();
                file = new File(file, "lang");
                file = new File(file, name + ".yml");
                try {
                        loadLocaleStream(new FileInputStream(file));
                } catch (FileNotFoundException fnfe) {
                        plugin.log("Unable to locate locale file: \"" + file.getPath() + "\"", Level.WARNING);
                        return;
                }
        }

        @SuppressWarnings("unchecked")
        private void loadLocaleStream(InputStream inp) {
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