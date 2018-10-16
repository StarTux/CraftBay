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
import edu.self.startux.craftBay.chat.ChatPlugin;
import edu.self.startux.craftBay.command.AuctionCommand;
import edu.self.startux.craftBay.locale.Color;
import edu.self.startux.craftBay.locale.Language;
import edu.self.startux.craftBay.locale.Message;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CraftBayPlugin extends JavaPlugin {
    private String tag = "[CraftBay]";
    private Economy economy;
    private ChatPlugin chatPlugin;
    private AuctionAnnouncer announcer;
    private AuctionHouse house;
    private AuctionScheduler scheduler;
    private AuctionCommand executor;
    private Language language;
    private AuctionLogger auctionLogger;
    private AuctionInventory inventory;
    private List<String> blacklistWorlds = new ArrayList<String>();
    private static CraftBayPlugin instance;
    private boolean debugMode = false;
    private GenericEventsHandler genericEventsHandler = null;

    public static CraftBayPlugin getInstance() {
        return instance;
    }

    public GenericEventsHandler getGenericEventsHandler() {
        return genericEventsHandler;
    }

    public void onEnable() {
        instance = this;
        Language.writeLanguageFiles();
        setupSerializations();
        executor = new AuctionCommand(this);
        getCommand("auction").setExecutor(executor);
        getCommand("bid").setExecutor(executor);
        if (!setupEconomy()) {
            getLogger().severe("Failed to setup economy. CraftBay is not enabled!");
            setEnabled(false);
            return;
        }
        if (getServer().getPluginManager().getPlugin("GenericEvents") != null) {
            genericEventsHandler = new GenericEventsHandler();
        }
        announcer = new AuctionAnnouncer(this);
        house = new AuctionHouse(this);
        scheduler = new AuctionScheduler(this);
        auctionLogger = new AuctionLogger(this);
        inventory = new AuctionInventory(this);
        scheduler.soon();
        loadAuctionConfig();
        auctionLogger.enable();
        house.enable();
        announcer.enable();
        scheduler.enable();
    }

    public void onDisable() {
        inventory.onDisable();
        if (scheduler != null) scheduler.disable();
        economy = null;
        if (chatPlugin != null) chatPlugin.disable();
        chatPlugin = null;
        announcer = null;
        house = null;
        scheduler = null;
        auctionLogger = null;
        instance = null;
    }

    public void setupSerializations() {
        ConfigurationSerialization.registerClass(TimedAuction.class);
        ConfigurationSerialization.registerClass(RealItem.class);
        ConfigurationSerialization.registerClass(FakeItem.class);
        ConfigurationSerialization.registerClass(Bid.class);
        ConfigurationSerialization.registerClass(PlayerMerchant.class);
        ConfigurationSerialization.registerClass(BankMerchant.class);
        ConfigurationSerialization.registerClass(ItemDelivery.class);
    }

    public void loadAuctionConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        reloadAuctionConfig();
    }

    public void reloadAuctionConfig() {
        reloadConfig();
        blacklistWorlds = getConfig().getStringList("blacklistworlds");
        language = new Language(this, getConfig().getString("lang"));
        tag = language.getMessage("Tag").toString();
        Color.configure(getConfig().getConfigurationSection("colors"));
        debugMode = getConfig().getBoolean("debug");
        announcer.reloadConfig();
        setupChat();
    }

    private void setupChat() {
        if (chatPlugin != null) chatPlugin.disable();
        do {
            // if all fails, fall back to bukkit chat
            chatPlugin = new BukkitChat(this);
            chatPlugin.enable(getConfig().getConfigurationSection("defaultchat"));
            getLogger().info("Falling back to default chat");
        } while (false);
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

    public AuctionInventory getAuctionInventory() {
        return inventory;
    }

    public List<String> getBlacklistWorlds() {
        return blacklistWorlds;
    }

    public void warn(CommandSender sender, Message msg) {
        List<String> lines = msg.compile();
        if (lines.isEmpty()) return;
        lines.set(0, Color.ERROR + tag + " " + lines.get(0));
        for (String line : lines) {
            sender.sendMessage(line);
        }
    }

    public void msg(CommandSender sender, List<String> lines) {
        if (lines.isEmpty()) return;
        lines.set(0, Color.DEFAULT + tag + " " + lines.get(0));
        for (String line : lines) {
            sender.sendMessage(line);
        }
    }

    public void msg(CommandSender sender, Message msg) {
        msg(sender, msg.compile());
    }

    public String getTag() {
        return tag;
    }

    public boolean getDebugMode() {
        return debugMode;
    }

    public void broadcast(Message msg) {
        List<String> lines = msg.compile();
        if (lines.isEmpty()) return;
        lines.set(0, Color.DEFAULT + tag + " " + lines.get(0));
        chatPlugin.broadcast(lines);
    }

    public ChatPlugin getChatPlugin() {
        return chatPlugin;
    }

    public Language getLanguage() {
        return language;
    }

    public Message getMessage(String key) {
        return language.getMessage(key);
    }

    public Message getMessages(String... keys) {
        return language.getMessages(keys);
    }
}
