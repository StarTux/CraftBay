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

import com.dthielke.herochat.Channel;
import com.dthielke.herochat.ChannelChatEvent;
import com.dthielke.herochat.ChannelManager;
import com.dthielke.herochat.Chatter;
import com.dthielke.herochat.Herochat;
import edu.self.startux.craftBay.CraftBayPlugin;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class HeroChat implements ChatPlugin {
	private Channel channel;
        private Herochat herochat;
        private CraftBayPlugin plugin;
        private String password = "";

        public HeroChat(CraftBayPlugin plugin) {
                this.plugin = plugin;
        }

        @Override
	public boolean enable(ConfigurationSection section) {
		String channelName = section.getString("channel");
		Plugin p = plugin.getServer().getPluginManager().getPlugin("Herochat");
                if (p == null) {
                        plugin.log("Herochat could not be found!", Level.WARNING);
                        return false;
                }
                if (p instanceof Herochat) {
                        herochat = (Herochat)p;
                } else {
                        plugin.log("Plugin `Herochat' is not what it is supposed to be!", Level.WARNING);
                        return false;
                }
                channel = herochat.getChannelManager().getChannel(channelName);
                if (channel == null) {
                        plugin.log("Channel `" + channelName + "' does not exist!", Level.WARNING);
                        return false;
                }
                password = section.getString("password");
                plugin.log("Herochat enabled. Using channel `" + channel.getName() + "'");
                return true;
        }

        @Override
        public void broadcast(List<String> lines) {
                // The announce() message turned out unreliable based on our tests
                for (Chatter chatter : channel.getMembers()) {
                        for (String line : lines) {
                                chatter.getPlayer().sendMessage(line);
                        }
                }
        }

        @Override
        public boolean listen(Player player, boolean on) {
                Chatter chatter = Herochat.getChatterManager().getChatter(player);
                if (on) {
                        if (chatter.canJoin(channel, password) == Chatter.Result.ALLOWED) {
                                return chatter.addChannel(channel, true, true);
                        }
                } else {
                        if (chatter.canLeave(channel) == Chatter.Result.ALLOWED) {
                                return chatter.removeChannel(channel, true, true);
                        }
                }
                return false;
        }

        @Override
        public boolean isListening(Player player) {
                return Herochat.getChatterManager().getChatter(player).hasChannel(channel);
        }
}