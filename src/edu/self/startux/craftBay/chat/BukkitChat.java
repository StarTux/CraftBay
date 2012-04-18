/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright 2012 StarTux
 *
 * This file is part of CraftBay.
 *
 * CraftBay is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
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

import java.util.HashSet;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import edu.self.startux.craftBay.CraftBayPlugin;

public class BukkitChat implements ChatPlugin {
        private HashSet<String> ignoreList = new HashSet<String>();
        CraftBayPlugin plugin;
        
        public BukkitChat(CraftBayPlugin plugin) {
                this.plugin = plugin;
        }

        @Override
        public boolean enable(ConfigurationSection section) {
                return true;
        }

        @Override
        public void broadcast(List<String> lines) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                        if (isListening(player)) {
                                for (String line : lines) {
                                        player.sendMessage(line);
                                }
                        }
                }
        }

        @Override
        public boolean listen(Player player, boolean on) {
                if (on) {
                        ignoreList.remove(player.getName().toLowerCase());
                } else {
                        ignoreList.add(player.getName().toLowerCase());
                }
                return true;
        }

        @Override
        public boolean isListening(Player player) {
                if (!player.hasPermission("auction.bid")) {
                        return false;
                }
                if (ignoreList.contains(player.getName().toLowerCase())) {
                        return false;
                }
                return true;
        }
}