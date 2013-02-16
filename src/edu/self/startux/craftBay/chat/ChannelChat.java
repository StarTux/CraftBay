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

import com.feildmaster.channelchat.channel.Channel;
import com.feildmaster.channelchat.channel.ChannelManager;
import edu.self.startux.craftBay.CraftBayPlugin;
import edu.self.startux.craftBay.locale.Message;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Provide ChannelChat functionality
 */
public class ChannelChat implements ChatPlugin {
        private Channel channel;
        private CraftBayPlugin plugin;

        public ChannelChat(CraftBayPlugin plugin) {
                this.plugin = plugin;
        }

        @Override
        public boolean enable(ConfigurationSection section) {
                String channelName = section.getString("channel");
		Plugin p = plugin.getServer().getPluginManager().getPlugin("ChannelChat");
                if (p == null) {
                        plugin.getLogger().warning("ChannelChat system could not be loaded!");
                        return false;
		}
                channel = ChannelManager.getManager().getChannel(channelName);
                if (channel == null) {
                        plugin.getLogger().warning("Channel `" + channelName + "' does not exist!");
                        return false;
                }
                plugin.getLogger().info("ChannelChat enabled. Using channel `" + channel.getName() + "'");
                return true;
        }

        @Override
        public void disable() {}

        @Override
        public void broadcast(List<String> lines) {
                for (String line : lines) {
                        channel.sendMessage(line);
                }
        }

        @Override
        public boolean listen(Player player, boolean on) {
                if (on) {
                        channel.addMember(player);
                        return true;
                } else {
                        channel.delMember(player);
                        return true;
                }
        }

        @Override
        public boolean isListening(Player player) {
                return channel.isMember(player);
        }
}
