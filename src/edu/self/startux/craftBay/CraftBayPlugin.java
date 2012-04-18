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

package edu.self.startux.craftBay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import net.milkbowl.vault.economy.Economy;
import edu.self.startux.craftBay.chat.BukkitChat;
import edu.self.startux.craftBay.chat.ChannelChat;
import edu.self.startux.craftBay.chat.ChatPlugin;
import edu.self.startux.craftBay.chat.HeroChat;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CraftBayPlugin extends JavaPlugin {
	private Auction auction;
	private Logger logger = null;
	private String tag = "[auction]";
	private Economy economy;
        private ChatPlugin chatPlugin;

	public void onDisable() {
                if (auction != null) {
                        auction.cancel(getServer().getConsoleSender());
                }
	}

	public void onEnable() {
                logger = Logger.getLogger("edu.self.startux.craftBay");
                try {
                        File dataFolder = getDataFolder();
                        if (!dataFolder.exists()) dataFolder.mkdir();
                        File logFile = new File(dataFolder, "auction.log");
                        logger.addHandler(new StreamHandler(new FileOutputStream(logFile, true), getFormatter()));
                } catch (IOException ioe) {
                        log("failed to open log file!", Level.WARNING);
                }
                AuctionCommand executor = new AuctionCommand(this);
		getCommand("auction").setExecutor(executor);
		getCommand("bid").setExecutor(executor);
		
		setupConfig();
                tag = getConfig().getString("tag");
		if (!setupEconomy()) {
			log("Economy Failure! Make sure you have an economy and Vault in your plugins folder!", Level.WARNING);
			setEnabled(false);
			return;
		}
                setupChat();
	}

        private Formatter getFormatter() {
                for (Logger logger = getLogger(); logger != null; logger = logger.getParent()) {
                        Handler[] handlers = logger.getHandlers();
                        if (handlers.length == 0) continue;
                        return handlers[0].getFormatter();
                }
                return new SimpleFormatter();
        }

        private void setupChat() {
                do {
                        if (getConfig().getBoolean("herochat.enable")) {
                                chatPlugin = new HeroChat(this);
                                if (chatPlugin.enable(getConfig().getConfigurationSection("herochat"))) {
                                        break;
                                }
                        }
                        if (getConfig().getBoolean("channelchat.enable")) {
                                chatPlugin = new ChannelChat(this);
                                if (chatPlugin.enable(getConfig().getConfigurationSection("channelchat"))) {
                                        break;
                                }
                        }
                        // if all fails, fall back to bukkit chat
                        chatPlugin = new BukkitChat(this);
                        log("Falling back to default chat");
                } while (false);
        }

	private void setupConfig() {
		FileConfiguration c = getConfig();
		c.addDefault("tag", "[auction]");
		c.addDefault("auctiontime", Integer.valueOf(900));
		c.addDefault("spaminterval", Integer.valueOf(120));
		c.addDefault("minincrement", Integer.valueOf(5));
		c.addDefault("herochat.enable", Boolean.valueOf(false));
		c.addDefault("herochat.channel", "Trade");
		c.addDefault("channelchat.enable", Boolean.valueOf(false));
		c.addDefault("channelchat.channel", "Trade");
		c.options().copyDefaults(true);
		saveConfig();
	}

	private Boolean setupEconomy()
        {
                RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
                if (economyProvider != null) {
                        economy = economyProvider.getProvider();
                }
                return (economy != null);
        }

	public Economy getEco() {
		return this.economy;
	}

	public Auction getAuction() {
		return this.auction;
	}

        public void setAuction(Auction auction) {
                this.auction = auction;
        }

	public void warn(CommandSender sender, String msg) {
		sender.sendMessage(AuctionColors.WARN + tag + " " + msg);
	}

        public void msg(CommandSender sender, List<String> lines) {
                if (lines.isEmpty()) return;
                lines.set(0, AuctionColors.FIELD + tag + " " + lines.get(0));
                for (String line : lines) {
                        sender.sendMessage(line);
                }
        }

	public void msg(CommandSender sender, String msg) {
		sender.sendMessage(AuctionColors.FIELD + tag + " " + msg);
	}

        public void log(String msg, Level level) {
                logger.log(level, "[CraftBay] " + msg);
        }

        public void log(String msg) {
                log(msg, Level.INFO);
        }

        public String getTag() {
                return tag;
        }

        public void broadcast(List<String> lines) {
                // for (String line : lines) {
                //         getServer().getConsoleSender().sendMessage(line);
                // }
                if (lines.isEmpty()) return;
                lines.set(0, AuctionColors.FIELD + tag + " " + lines.get(0));
                chatPlugin.broadcast(lines);
        }

	public void broadcast(String msg) {
                List<String> list = new ArrayList<String>(1);
                list.add(msg);
                broadcast(list);
	}

        public ChatPlugin getChatPlugin() {
                return chatPlugin;
        }
}
