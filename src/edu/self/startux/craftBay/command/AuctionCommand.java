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

package edu.self.startux.craftBay.command;

import edu.self.startux.craftBay.Auction;
import edu.self.startux.craftBay.AuctionState;
import edu.self.startux.craftBay.BankMerchant;
import edu.self.startux.craftBay.CraftBayPlugin;
import edu.self.startux.craftBay.FakeItem;
import edu.self.startux.craftBay.HandItem;
import edu.self.startux.craftBay.Item;
import edu.self.startux.craftBay.Merchant;
import edu.self.startux.craftBay.MoneyAmount;
import edu.self.startux.craftBay.PlayerMerchant;
import edu.self.startux.craftBay.RealItem;
import edu.self.startux.craftBay.chat.ChatPlugin;
import edu.self.startux.craftBay.event.AuctionCancelEvent;
import edu.self.startux.craftBay.event.AuctionTimeChangeEvent;
import edu.self.startux.craftBay.locale.Message;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AuctionCommand extends AuctionParameters implements CommandExecutor {
	public AuctionCommand(CraftBayPlugin plugin) {
                super(plugin);
	}

        @Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] argv) {
                if (sender instanceof Player && plugin.getBlacklistWorlds().contains(((Player)sender).getWorld().getName()) && !((Player)sender).hasPermission("auction.admin")) {
                        plugin.warn(sender, plugin.getMessage("command.BadWorld").set("cmd", cmd));
                        return true;
                }
                LinkedList<String> argl = new LinkedList<String>(Arrays.asList(argv));
                if (label.equals("bid")) {
                        callCommand(sender, "bid", argl);
                        return true;
                }
                if (argl.isEmpty()) {
                        Auction auction = plugin.getAuction();
                        if (auction != null) {
                                info(sender, auction);
                        } else {
                                help(sender);
                        }
                        return true;
                }
                String token = argl.getFirst();
                argl.removeFirst();
                callCommand(sender, token, argl);
                return true;
        }

        @SubCommand(perm = "info", shortcut = true)
        public void info(CommandSender sender, Auction auction) {
                plugin.msg(sender, plugin.getMessages("auction.info.Header", "auction.info.Owner", (auction.getItem() instanceof FakeItem ? "auction.info.FakeItem" : "auction.info.RealItem"), (auction.getWinner() != null ? "auction.info.Winner" : "auction.info.NoWinner"), (sender instanceof Player && auction.getWinner() != null && auction.getWinner().equals(PlayerMerchant.getByPlayer((Player)sender)) ? "auction.info.Self" : null), (auction.getState() == AuctionState.RUNNING ? "auction.info.Time" : "auction.info.State"), "auction.info.Help").set(auction, sender));
        }

        @SubCommand(perm = "bid", shortcut = true, optional = 1)
        public void bid(Player player, Auction auction, Integer amount) {
                if (amount == null) amount = auction.getMinimalBid();
                if (amount < 0) {
                        plugin.warn(player, plugin.getMessage("commands.bid.BidNegative").set(auction, player).set("arg", amount));
                        return;
                }
                auction.bid(PlayerMerchant.getByPlayer(player), amount);
        }
        
        @SubCommand(perm = "start", shortcut = true, optional = 1)
        public void end(CommandSender sender, Auction auction, Integer delay) {
                if (!auction.getOwner().equals(new PlayerMerchant(sender.getName()))) {
                        if (!sender.hasPermission("auction.admin") && !sender.isOp()) {
                                plugin.warn(sender, plugin.getMessage("commands.end.NotOwner").set(auction, sender));
                                return;
                        }
                }
                if (delay == null) delay = 0;
                if (delay < 0) {
                        plugin.warn(sender, plugin.getMessage("commands.end.DelayNegative").set(auction, sender).set("arg", delay));
                        return;
                }
                if (delay > auction.getTimeLeft()) {
                        if (!sender.hasPermission("auction.admin") && !sender.isOp()) {
                                plugin.warn(sender, plugin.getMessage("commands.end.DelayTooLong").set(auction, sender).set("arg", delay));
                                return;
                        }
                }
                AuctionTimeChangeEvent event = new AuctionTimeChangeEvent(auction, sender, delay);
                plugin.getServer().getPluginManager().callEvent(event);
                auction.setTimeLeft(delay);
        }

        @SubCommand(perm = "start", shortcut = true, optional = 1)
        public void cancel(CommandSender sender, Auction auction, Integer id) {
                if (id != null) {
                        auction = plugin.getAuctionScheduler().getById(id);
                        if (auction == null) {
                                plugin.warn(sender, plugin.getMessage("command.NoSuchAuction").set(sender).set("arg", id));
                                return;
                        }
                }
                if (!auction.getOwner().equals(new PlayerMerchant(sender.getName())) && !sender.hasPermission("auction.admin") && !sender.isOp()) {
                        plugin.warn(sender, plugin.getMessage("commands.cancel.NotOwner").set(auction, sender));
                        return;
                }
                if (auction.getState() == AuctionState.RUNNING && !sender.hasPermission("auction.admin") && !sender.isOp()) {
                        plugin.warn(sender, plugin.getMessage("commands.cancel.Running").set(auction, sender));
                        return;
                }
                if (auction.getState() == AuctionState.CANCELED) {
                        plugin.warn(sender, plugin.getMessage("commands.cancel.Canceled").set(auction, sender));
                        return;
                }
                if (auction.getState() == AuctionState.ENDED) {
                        plugin.warn(sender, plugin.getMessage("commands.cancel.Ended").set(auction, sender));
                        return;
                }
                AuctionCancelEvent event = new AuctionCancelEvent(auction, sender);
                plugin.getServer().getPluginManager().callEvent(event);
                auction.cancel();
                plugin.getAuctionHouse().cancelAuction(auction);
        }

        @SubCommand(perm = "admin", optional = 1)
        public void bankBid(CommandSender sender, Auction auction, Integer amount) {
                if (amount == null) amount = auction.getMinimalBid();
                if (amount < 0) {
                        plugin.warn(sender, plugin.getMessage("commands.bankbid.BidNegative").set(auction, sender).set("arg", amount));
                        return;
                }
                auction.bid(BankMerchant.getInstance(), amount);
        }
        
        @SubCommand(perm = "info")
        public void listen(Player player) {
                ChatPlugin chatPlugin = plugin.getChatPlugin();
                if (chatPlugin.isListening(player)) {
                        plugin.msg(player, plugin.getMessage("commands.listen.AlreadyListen").set(player));
                        return;
                }
                boolean ret = plugin.getChatPlugin().listen(player, true);
                if (!ret) {
                        plugin.msg(player, plugin.getMessage("commands.listen.ListenError").set(player));
                        return;
                }
                plugin.msg(player, plugin.getMessage("commands.listen.ListenSuccess").set(player));
        }

        @SubCommand(perm = "info")
        public void ignore(Player player) {
                ChatPlugin chatPlugin = plugin.getChatPlugin();
                if (!chatPlugin.isListening(player)) {
                        plugin.msg(player, plugin.getMessage("commands.listen.AlreadyIgnore").set(player));
                        return;
                }
                boolean ret = plugin.getChatPlugin().listen(player, false);
                if (!ret) {
                        plugin.msg(player, plugin.getMessage("commands.listen.IgnoreError").set(player));
                        return;
                }
                plugin.msg(player, plugin.getMessage("commands.listen.IgnoreSuccess").set(player));
        }

        @SubCommand(perm = "start", shortcut = true, optional = 3)
        public void start(Player player, ItemStack stack, Integer amount, Integer price) {
                if (player.getGameMode() == GameMode.CREATIVE && plugin.getConfig().getBoolean("denycreative")) {
                        plugin.warn(player, plugin.getMessage("commands.start.CreativeDenial").set(player));
                        return;
                }
                if (stack == null) {
                        price = plugin.getConfig().getInt("startingbid");
                        plugin.getAuctionInventory().initPlayer(player, price);
                        return;
                }
                if (amount == null) amount = 1;
                if (amount < 1) {
                        plugin.warn(player, plugin.getMessage("commands.start.AmountTooSmall").set(player).set("arg", amount));
                        return;
                }
                stack.setAmount(amount);
                Merchant merchant = PlayerMerchant.getByPlayer(player);
                Item item = new RealItem(stack);
                if (price == null) price = 0;
                if (price < 0) {
                        plugin.warn(player, plugin.getMessage("commands.start.PriceTooLow").set(player).set("arg", price));
                        return;
                }
                Auction auction = plugin.getAuctionHouse().createAuction(merchant, item, price);
                if (auction != null) {
                        merchant.msg(plugin.getMessage("commands.start.Success").set(auction, merchant));
                }
        }

        @SubCommand(perm = "admin", optional = 3)
        public void bank(CommandSender sender, ItemStack stack, Integer amount, Integer price, Integer time) {
                if (amount == null) amount = 1;
                if (amount < 1) {
                        plugin.warn(sender, plugin.getMessage("commands.start.AmountTooSmall").set(sender).set("arg", amount));
                        return;
                }
                stack.setAmount(amount);
                Merchant merchant = BankMerchant.getInstance();
                RealItem item = new RealItem(stack);
                if (price == null) price = 0;
                if (price < 0) {
                        plugin.warn(sender, plugin.getMessage("commands.start.PriceTooLow").set(sender).set("arg", amount));
                        return;
                }
                Auction auction = plugin.getAuctionHouse().createAuction(merchant, item, price);
                if (auction == null) return;
                if (time != null) {
                        if (time < 0) {
                                plugin.warn(sender, plugin.getMessage("commands.bank.TimeNegative").set(sender).set("arg", time));
                                // go on
                        } else {
                                auction.setTimeLeft(time);
                        }
                }
                plugin.msg(sender, plugin.getMessage("commands.start.Success").set(auction, sender));
        }

        @SubCommand(perm = "start", optional = 2)
        public void hand(Player player, Integer amount, Integer price) {
                if (player.getGameMode() == GameMode.CREATIVE && plugin.getConfig().getBoolean("denycreative")) {
                        plugin.warn(player, plugin.getMessage("commands.start.CreativeDenial").set(player));
                        return;
                }
                HandItem item;
                try {
                        item = new HandItem(player);
                } catch (IllegalArgumentException iae) {
                        plugin.warn(player, plugin.getMessage("commands.start.HandEmpty"));
                        return;
                }
                if (amount != null) {
                        if (amount < 1) {
                                plugin.warn(player, plugin.getMessage("commands.start.AmountTooSmall").set(player).set("arg", amount));
                                return;
                        }
                        item.setAmount(amount);
                }
                if (price == null) price = 0;
                if (price < 0) {
                        plugin.warn(player, plugin.getMessage("commands.start.PriceTooLow").set(player).set("arg", price));
                        return;
                }
                Merchant merchant = PlayerMerchant.getByPlayer(player);
                Auction auction = plugin.getAuctionHouse().createAuction(merchant, item, price);
                if (auction != null) {
                        merchant.msg(plugin.getMessage("commands.start.Success").set(auction, merchant));
                }
        }

        @SubCommand(perm = "info", optional = 1, aliases = { "hist" })
        public void history(CommandSender sender, Integer id) {
                if (id == null) {
                        LinkedList<Message> msgs = new LinkedList<Message>();
                        for (Auction auction : plugin.getAuctionScheduler().getQueue()) {
                                msgs.addFirst(plugin.getMessage("history.Queue").set(auction));
                        }
                        {
                                Auction auction = plugin.getAuctionScheduler().getCurrentAuction();
                                if (auction != null) {
                                        msgs.addFirst(plugin.getMessage("history.Current").set(auction));
                                }
                        }
                        for (Auction auction : plugin.getAuctionScheduler().getHistory()) {
                                msgs.addFirst(plugin.getMessage("history.History").set(auction));
                        }
                        List<String> out = new ArrayList<String>(msgs.size());
                        out.addAll(plugin.getMessage("history.Header").set(sender).compile());
                        for (Message msg : msgs) out.addAll(msg.compile());
                        plugin.msg(sender, out);
                        return;
                }
                Auction auction = plugin.getAuctionScheduler().getById(id);
                if (auction == null) {
                        plugin.warn(sender, plugin.getMessage("commands.history.NoEntry").set(sender).set("id", id));
                        return;
                }
                plugin.msg(sender, plugin.getMessages("auction.info.Header", "auction.info.Owner", (auction.getItem() instanceof FakeItem ? "auction.info.FakeItem" : "auction.info.RealItem"), (auction.getWinner() != null ? "auction.info.Winner" : "auction.info.NoWinner"), "auction.info.State").set(auction, sender));
        }

        @SubCommand(aliases = { "h", "?" })
        public void help(CommandSender sender) {
                String[] cmds = { "Header", "Help", "Info", "History", "Bid", "BidShort", "Start", "Hand", "Gui", "End", "Cancel", "Listen" };
                Message msg = new Message();
                int fee = plugin.getConfig().getInt("auctionfee");
                int tax = plugin.getConfig().getInt("auctiontax");
                int minbid = plugin.getConfig().getInt("startingbid");
                for (String cmd : cmds) msg.append(plugin.getMessage("help." + cmd));
                if (fee > 0) msg.append(plugin.getMessage("help.Fee"));
                if (tax > 0) msg.append(plugin.getMessage("help.Tax"));
                if (sender.hasPermission("auction.admin") || sender.isOp()) {
                        String[] admcmds = { "Bank", "Fake", "BankBid", "Log", "Reload" };
                        for (String cmd : admcmds) msg.append(plugin.getMessage("adminhelp." + cmd));
                }
                plugin.msg(sender, msg.set("fee", new MoneyAmount(fee)).set("tax", tax).set("minbid", new MoneyAmount(minbid)));
        }

        @SubCommand(perm = "admin")
        public void reload(CommandSender sender) {
                plugin.reloadAuctionConfig();
                sender.sendMessage("config file reloaded.");
        }

        @SubCommand(perm = "admin", optional = 1)
        public void log(CommandSender sender, Integer id) {
                Auction auction;
                if (id != null) {
                        auction = plugin.getAuctionScheduler().getById(id);
                        if (auction == null) {
                                plugin.warn(sender, plugin.getMessage("command.NoSuchAuction").set(sender).set("arg", id));
                                return;
                        }
                } else {
                        auction = plugin.getAuctionScheduler().getCurrentAuction();
                        if (auction == null) {
                                plugin.warn(sender, plugin.getMessage("command.NoCurrentAuction"));
                                return;
                        }
                }
                List<String> lines = new LinkedList<String>();
                lines.addAll(plugin.getMessage("log.Header").set(auction, sender).compile());
                for (String log : auction.getLog()) {
                        lines.addAll(plugin.getMessage("log.Log").set(auction, sender).set("log", log).compile());
                }
                plugin.msg(sender, lines);
        }

        @SubCommand(perm = "admin", optional = 2)
        public void fake(CommandSender sender, String title, Integer price, Integer duration) {
                FakeItem item = new FakeItem(title);
                if (price == null) price = plugin.getConfig().getInt("startingbid");
                if (duration == null) duration = plugin.getConfig().getInt("auctiontime");
                Auction auction = plugin.getAuctionHouse().createAuction(BankMerchant.getInstance(), item, price);
                if (auction == null) {
                        plugin.msg(sender, plugin.getMessage("commands.fake.Fail").set(sender));
                        return;
                }
                auction.setTimeLeft(duration);
                plugin.msg(sender, plugin.getMessage("commands.fake.Success").set(sender));
        }

        @SubCommand(perm = "start", optional = 1)
        public void gui(Player player, Integer minbid) {
                if (player.getGameMode() == GameMode.CREATIVE && plugin.getConfig().getBoolean("denycreative")) {
                        plugin.warn(player, plugin.getMessage("commands.start.CreativeDenial").set(player));
                        return;
                }
                if (minbid == null) minbid = plugin.getConfig().getInt("startingbid");
                if (minbid < 0) {
                        plugin.warn(player, plugin.getMessage("commands.start.PriceTooLow").set(player).set("arg", minbid));
                        return;
                }
                plugin.getAuctionInventory().initPlayer(player, minbid);
        }
}
