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

package edu.self.startux.craftBay.chat;

import edu.self.startux.craftBay.CraftBayPlugin;
import edu.self.startux.craftBay.Msg;
import edu.self.startux.craftBay.locale.Message;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class BukkitChat implements ChatPlugin {
    private CraftBayPlugin plugin;
    private boolean whitelisted;
    private HashSet<String> playerList = new HashSet<String>();
    private FileConfiguration conf = new YamlConfiguration(); 
    private static final String CONFIG_FILE_PATH = "defaultchat.yml";
        
    public BukkitChat(CraftBayPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean enable(ConfigurationSection section) {
        whitelisted = !section.getBoolean("autojoin", true);
        try {
            conf.load(new File(plugin.getDataFolder(), CONFIG_FILE_PATH));
        } catch (FileNotFoundException fnfe) {
            // do nothing
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (whitelisted) {
            for (String name : conf.getStringList("whitelist")) playerList.add(name);
        } else {
            for (String name : conf.getStringList("blacklist")) playerList.add(name);
        }
        return true;
    }

    @Override
    public void disable() {
        if (whitelisted) {
            conf.set("whitelist", new ArrayList<String>(playerList));
        } else {
            conf.set("blacklist", new ArrayList<String>(playerList));
        }
        try {
            conf.save(new File(plugin.getDataFolder(), CONFIG_FILE_PATH));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void broadcast(List<String> lines) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (isListening(player)) {
                for (String line : lines) {
                    Msg.raw(player,
                            Msg.button(ChatColor.DARK_AQUA,
                                       line, null,
                                       "&9/auc\n&rAuction info",
                                       "/auc"));
                }
            }
        }
    }

    @Override
    public boolean listen(Player player, boolean on) {
        if (whitelisted) {
            if (on) playerList.add(player.getName().toLowerCase());
            else playerList.remove(player.getName().toLowerCase());
        } else {
            if (on) playerList.remove(player.getName().toLowerCase());
            else playerList.add(player.getName().toLowerCase());
        }
        return true;
    }

    @Override
    public boolean isListening(Player player) {
        if (!player.hasPermission("auction.bid")) {
            return false;
        }
        if (!whitelisted && !playerList.contains(player.getName().toLowerCase())) return true;
        else if (whitelisted && playerList.contains(player.getName().toLowerCase())) return true;
        return false;
    }
}
