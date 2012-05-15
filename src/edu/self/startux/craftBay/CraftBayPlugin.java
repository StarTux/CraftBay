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

package edu.self.startux.craftBay;

import edu.self.startux.craftBay.chat.BukkitChat;
import edu.self.startux.craftBay.chat.ChannelChat;
import edu.self.startux.craftBay.chat.ChatPlugin;
import edu.self.startux.craftBay.chat.HeroChat;
import edu.self.startux.craftBay.locale.Locale;
import edu.self.startux.craftBay.locale.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CraftBayPlugin extends JavaPlugin {
	private Logger logger;
	private String tag = "[CraftBay]";
	private Economy economy;
        private ChatPlugin chatPlugin;
        private AuctionAnnouncer announcer;
        private AuctionHouse house;
        private AuctionScheduler scheduler;
        private AuctionCommand executor;
        private Locale locale;
        private static CraftBayPlugin instance;
        
        public static CraftBayPlugin getInstance() {
                return instance;
        }

	public void onEnable() {
                instance = this;
                logger = Logger.getLogger("edu.self.startux.craftBay");
                ConfigurationSerialization.registerClass(TimedAuction.class);
                ConfigurationSerialization.registerClass(RealItem.class);
                ConfigurationSerialization.registerClass(FakeItem.class);
                ConfigurationSerialization.registerClass(Bid.class);
                ConfigurationSerialization.registerClass(PlayerMerchant.class);
                ConfigurationSerialization.registerClass(BankMerchant.class);
                executor = new AuctionCommand(this);
		getCommand("auction").setExecutor(executor);
		getCommand("bid").setExecutor(executor);
		setupConfig();
		if (!setupEconomy()) {
			log("Failed to setup economy. CraftBay is not enabled!", Level.SEVERE);
			setEnabled(false);
			return;
		}
                setupChat();
                locale = new Locale(this, "en_US");
                tag = locale.getMessage("tag").toString();
                announcer = new AuctionAnnouncer(this);
                announcer.enable();
                house = new AuctionHouse(this);
                house.enable();
                scheduler = new AuctionScheduler(this);
                scheduler.enable();
                scheduler.soon();
	}

	public void onDisable() {
                scheduler.disable();
                logger = null;
                economy = null;
                chatPlugin = null;
                announcer = null;
                house = null;
                scheduler = null;
                instance = null;
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
		getConfig().options().copyDefaults(true);
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
		return scheduler.getCurrentAuction();
	}

        public AuctionHouse getAuctionHouse() {
                return house;
        }

        public AuctionScheduler getAuctionScheduler() {
                return scheduler;
        }

        public void warn(CommandSender sender, List<String> lines) {
                if (lines.isEmpty()) return;
                lines.set(0, Color.ERROR + tag + " " + lines.get(0));
                for (String line : lines) {
                        sender.sendMessage(line);
                }
        }

	public void warn(CommandSender sender, String msg) {
		sender.sendMessage(Color.ERROR + tag + " " + msg);
	}

        public void msg(CommandSender sender, List<String> lines) {
                if (lines.isEmpty()) return;
                lines.set(0, Color.DEFAULT + tag + " " + lines.get(0));
                for (String line : lines) {
                        sender.sendMessage(line);
                }
        }

	public void msg(CommandSender sender, String msg) {
		sender.sendMessage(Color.DEFAULT + tag + " " + msg);
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
                lines.set(0, Color.DEFAULT + tag + " " + lines.get(0));
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

        public Locale getLocale() {
                return locale;
        }
}
