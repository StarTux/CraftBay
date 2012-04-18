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

import edu.self.startux.craftBay.chat.ChatPlugin;
import java.util.Arrays;
import java.util.Iterator;
import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AuctionCommand implements CommandExecutor {
	private CraftBayPlugin plugin;
        private AuctionColors col = new AuctionColors();

	AuctionCommand(CraftBayPlugin plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] argv) {
                Iterator<String> args = Arrays.asList(argv).iterator();
                if (label.equals("bid")) {
                        bid(sender, args);
                        return true;
                }
                // label is either "auc" or "auction"
                if (!args.hasNext()) {
                        if (plugin.getAuction() != null) {
                                info(sender, args);
                        } else {
                                help(sender, args);
                        }
                        return true;
                }
                String arg = args.next();
                if (arg.equalsIgnoreCase("start") || arg.equalsIgnoreCase("s")) {
                        start(sender, args);
                        return true;
                }
                if (arg.equalsIgnoreCase("hand") || arg.equalsIgnoreCase("h")) {
                        hand(sender, args);
                        return true;
                }
                if (arg.equalsIgnoreCase("?")) {
                        help(sender, args);
                        return true;
                }
                if (arg.equalsIgnoreCase("end")
                    || arg.equalsIgnoreCase("e")) {
                        end(sender, args);
                        return true;
                }
                if (arg.equalsIgnoreCase("cancel")
                    || arg.equalsIgnoreCase("c")) {
                        cancel(sender, args);
                        return true;
                }
                if (arg.equalsIgnoreCase("info")
                    || arg.equalsIgnoreCase("i")) {
                        info(sender, args);
                        return true;
                }
                if (arg.equalsIgnoreCase("bid")
                    || arg.equalsIgnoreCase("b")) {
                        bid(sender, args);
                        return true;
                }
                if (arg.equalsIgnoreCase("listen")) {
                        listen(sender, args, true);
                        return true;
                }
                if (arg.equalsIgnoreCase("ignore")) {
                        listen(sender, args, false);
                        return true;
                }
                if (arg.equalsIgnoreCase("bank")) {
                        bankAuction(sender, args);
                        return true;
                }
                if (arg.equalsIgnoreCase("bankbid")) {
                        bankBid(sender, args);
                        return true;
                }
                if (arg.equalsIgnoreCase("spam")) {
                        spam(sender, args);
                        return true;
                }
                if (arg.equalsIgnoreCase("admininfo")) {
                        adminInfo(sender, args);
                        return true;
                }
                plugin.warn(sender, "No such command: \"" + arg + "\"!");
                return true;
        }

        private void info(CommandSender sender, Iterator<String> args) {
                if (!sender.hasPermission("auction.info") && !sender.isOp()) {
                        this.plugin.warn(sender, "You do not have permission!");
                        return;
                }
                if (args.hasNext()) {
                        this.plugin.warn(sender, "Too many arguments!");
                        return;
                }
                Auction auction = plugin.getAuction();
                if (auction == null) {
                        plugin.warn(sender, "No auction running!");
                        return;
                }
                auction.info(sender);
        }

        private void adminInfo(CommandSender sender, Iterator<String> args) {
                if (!sender.hasPermission("auction.admin") && !sender.isOp()) {
                        this.plugin.warn(sender, "You do not have permission!");
                        return;
                }
                if (args.hasNext()) {
                        this.plugin.warn(sender, "Too many arguments!");
                        return;
                }
                Auction auction = plugin.getAuction();
                if (auction == null) {
                        plugin.warn(sender, "No auction running!");
                        return;
                }
                auction.adminInfo(sender);
        }
        
        private void bid(CommandSender sender, Iterator<String> args) {
                Player player;
                if (sender instanceof Player) {
                        player = (Player)sender;
                } else {
                        plugin.warn(sender, "Only players can bid!");
                        return;
                }
                if (!sender.hasPermission("auction.bid") && !sender.isOp()) {
                        plugin.warn(sender, "You do not have permission!");
                        return;
                }
                Auction auction = this.plugin.getAuction();
                if (auction == null) {
                        plugin.warn(sender, "No auction running!");
                        return;
                }
                int bid = 0;
                if (!args.hasNext()) {
                        bid = auction.getMinimalBid();
                } else {
                        String arg = args.next();
                        try {
                                bid = Integer.parseInt(arg);
                        } catch (NumberFormatException nfe) {
                                if (arg.contains(",") || arg.contains(".")) {
                                        // a lot of people try seperators or comma amounts
                                        plugin.warn(sender, "No dots, no commas, only full amounts!");
                                        return;
                                }
                                plugin.warn(sender, "Invalid amount argument: \"" + arg + "\"!");
                                return;
                        }
                        if (bid < 0) {
                                plugin.warn(sender, "Invalid amount!");
                                return;
                        }
                }
                if (args.hasNext()) {
                        plugin.warn(sender, "Too many arguments!");
                        return;
                }
                auction.bid(new PlayerMerchant(plugin, player), bid);
        }
        
        private void end(CommandSender sender, Iterator<String> args) {
                Auction auction = plugin.getAuction();
                if (auction == null) {
                        plugin.warn(sender, "No auction running!");
                        return;
                }
                if (!auction.getOwner().equals(sender)) {
                        if (!sender.hasPermission("auction.admin") && !sender.isOp()) {
                                plugin.warn(sender, "You are not the owner of this auction!");
                                return;
                        }
                }
                int time = 0;
                // delay, optional
                if (args.hasNext()) {
                        String arg = args.next();
                        try {
                                time = Integer.parseInt(arg);
                        } catch (NumberFormatException nfe) {
                                plugin.warn(sender, "Bad time argument: " + arg);
                                return;
                        }
                        if (time < 0) {
                                plugin.warn(sender, "Time can't be negative!");
                                return;
                        }
                }
                if (args.hasNext()) {
                        plugin.warn(sender, "Too many arguments!");
                        return;
                }
                if (time == 0) {
                        plugin.getAuction().stop();
                        return;
                }
                if (time > plugin.getAuction().getTimeLeft()) {
                        if (!sender.hasPermission("auction.admin") && !sender.isOp()) {
                                plugin.warn(sender, "Only admins can increase the duration of an auction!");
                                return;
                        }
                }
                plugin.getAuction().setTimeLeft(time);
        }

        private void cancel(CommandSender sender, Iterator<String> args) {
                Auction auction = plugin.getAuction();
                if (auction == null) {
                        plugin.warn(sender, "No auction running!");
                        return;
                }
                if (!sender.hasPermission("auction.admin") && !sender.isOp()) {
                        this.plugin.warn(sender, "You do not have permission!");
                        return;
                }
                if (args.hasNext()) {
                        plugin.warn(sender, "Too many arguments!");
                        return;
                }
                auction.cancel(sender);
        }

        private void bankBid(CommandSender sender, Iterator<String> args) {
                if (!sender.hasPermission("auction.admin") && !sender.isOp()) {
                        plugin.warn(sender, "You do not have permission!");
                        return;
                }
                Auction auction = plugin.getAuction();
                if (auction == null) {
                        plugin.warn(sender, "No running auction!");
                        return;
                }
                int bid;
                if (args.hasNext()) {
                        String arg = args.next();
                        try {
                                bid = Integer.parseInt(arg);
                        } catch (NumberFormatException nfe) {
                                plugin.warn(sender, "Invalid amount argument: " + arg);
                                return;
                        }
                        if (bid < 0) {
                                plugin.warn(sender, "Invalid amount!");
                                return;
                        }
                } else {
                        bid = auction.getMinimalBid();
                }
                if (args.hasNext()) {
                        plugin.warn(sender, "Too many arguments!");
                        return;
                }
                boolean ret = auction.bid(BankMerchant.getInstance(), bid);
                if (!ret) {
                        plugin.warn(sender, "Bank bid failed!");
                        return;
                }
        }

        private void listen(CommandSender sender, Iterator<String> args, boolean on) {
                if (args.hasNext()) {
                        plugin.warn(sender, "Too many arguments!");
                        return;
                }
                if (sender instanceof Player) {
                        Player player = (Player)sender;
                        ChatPlugin chatPlugin = plugin.getChatPlugin();
                        if (on && chatPlugin.isListening(player)) {
                                plugin.msg(sender, "You are already listening to auctions!");
                                return;
                        } else if (!on && !chatPlugin.isListening(player)) {
                                plugin.msg(sender, "You are already ignoring auctions!");
                                return;
                        }
                        boolean ret = plugin.getChatPlugin().listen((Player)sender, on);
                        if (!ret) {
                                plugin.warn(sender, "An error occured trying to perform this command");
                                return;
                        }
                        if (on) {
                                plugin.msg(sender, "Listening to auctions");
                        } else {
                                plugin.msg(sender, "Ignoring auctions");
                        }
                } else {
                        plugin.warn(sender, "Only players can do that!");
                        return;
                }
        }

        private ItemStack itemByString(String name) {
                ItemInfo item = Items.itemByString(name);
                if (item == null) return null;
                return item.toStack();
        }

        private void bankAuction(CommandSender sender, Iterator<String> args) {
                if (this.plugin.getAuction() != null) {
                        this.plugin.warn(sender, "There is already an auction running!");
                        return;
                }
                if (!sender.hasPermission("auction.admin") && !sender.isOp()) {
                        this.plugin.warn(sender, "You do not have permission!");
                        return;
                }
                ItemStack lot = null;
                String comment = null;
                int price = plugin.getConfig().getInt("minincrement");
                int time = plugin.getConfig().getInt("auctiontime");
                // item
                if (args.hasNext()) {
                        String arg = args.next();
                        if (arg.startsWith("\"")) {
                                // fake item
                                comment = arg;
                                if (!arg.endsWith("\"")) {
                                        while (args.hasNext()) {
                                                arg = args.next();
                                                comment += " " + arg;
                                                if (arg.endsWith("\"")) break;
                                        }
                                }
                                if (!comment.endsWith("\"")) {
                                        plugin.warn(sender, "Syntax error: No matching paranthesis!");
                                        return;
                                }
                                comment = comment.substring(1, comment.length() - 1);
                        } else {
                                // real spawned item
                                lot = itemByString(arg);
                                if (lot == null) {
                                        plugin.warn(sender, "Invalid item argument: \"" + arg + "\"!");
                                        return;
                                }
                                // amount
                                if (args.hasNext()) {
                                        arg = args.next();
                                        int amount = 1;
                                        try {
                                                amount = Integer.parseInt(arg);
                                        } catch (NumberFormatException nfe) {
                                                plugin.warn(sender, "Invalid amount argument: \"" + arg + "\"!");
                                                return;
                                        }
                                        if (amount < 1) {
                                                plugin.warn(sender, "Invalid amount!");
                                                return;
                                        }
                                        lot.setAmount(amount);
                                } else {
                                        plugin.warn(sender, "Not enough arguments!");
                                        return;
                                }
                        }
                } else {
                        plugin.warn(sender, "Not enough arguments!");
                        return;
                }
                // starting bid, optional
                if (args.hasNext()) {
                        String arg = args.next();
                        try {
                                price = Integer.parseInt(arg);
                        } catch (NumberFormatException nfe) {
                                plugin.warn(sender, "Invalid amount argument: \"" + arg + "\"!");
                                return;
                        }
                        if (price < 0) {
                                plugin.warn(sender, "Invalid starting bid!");
                                return;
                        }
                }
                // time, optional
                if (args.hasNext()) {
                        String arg = args.next();
                        try {
                                time = Integer.parseInt(arg);
                        } catch (NumberFormatException nfe) {
                                plugin.warn(sender, "Invalid time argument: \"" + arg + "\"!");
                                return;
                        }
                        if (time < 0) {
                                plugin.warn(sender, "Invalid auction time!");
                                return;
                        }
                }
                if (args.hasNext()) {
                        plugin.warn(sender, "Too many arguments!");
                        return;
                }
                Auction auction = new Auction(plugin, lot, BankMerchant.getInstance(), time, price);
                if (lot == null) {
                        auction.setComment(comment);
                }
                auction.start();
        }
        
        private void start(CommandSender sender, Iterator<String> args) {
                if (!(sender instanceof Player)) {
                        plugin.warn(sender, "Only players can start an auction!");
                        return;
                }
                if (!sender.hasPermission("auction.start") && !sender.isOp()) {
                        this.plugin.warn(sender, "You do not have permission!");
                        return;
                }
                if (this.plugin.getAuction() != null) {
                        this.plugin.warn(sender, "There is already an auction Running");
                        return;
                }
                ItemStack lot = null;
                // item
                if (args.hasNext()) {
                        String arg = args.next();
                        lot = itemByString(arg);
                        if (lot == null || lot.getType().equals(Material.AIR)) {
                                this.plugin.warn(sender, "Invalid Item: \"" + arg + "\"!");
                                return;
                        }
                } else {
                        plugin.warn(sender, "Not enough arguments!");
                        return;
                }
                // amount, optional
                if (args.hasNext()) {
                        String arg = args.next();
                        int amount;
                        try {
                                amount = Integer.parseInt(arg);
                        } catch (NumberFormatException nfe) {
                                plugin.warn(sender, "Invalid amount argument: " + arg);
                                return;
                        }
                        if (amount < 1) {
                                this.plugin.warn(sender, "Invalid amount!");
                                return;
                        }
                        lot.setAmount(amount);
                }
                // starting bid, optional
                int price = plugin.getConfig().getInt("minincrement");
                if (args.hasNext()) {
                        String arg = args.next();
                        try {
                                price = Integer.parseInt(arg);
                        } catch (NumberFormatException e) {
                                this.plugin.warn(sender, "Invalid price argument: " + arg);
                                return;
                        }
                        if (price < 0) {
                                this.plugin.warn(sender, "Invalid price");
                                return;
                        }
                }
                if (args.hasNext()) {
                        plugin.warn(sender, "Too many arguments!");
                        return;
                }
                Auction auction = new Auction(plugin, lot, new PlayerMerchant(plugin, (Player)sender), plugin.getConfig().getInt("auctiontime"), price);
                auction.start();
        }

        private void hand(CommandSender sender, Iterator<String> args) {
                if (!sender.hasPermission("auction.start")) {
                        plugin.warn(sender, "You don't have permission!");
                        return;
                }
                if (!(sender instanceof Player)) {
                        plugin.warn(sender, "Only players can start auctions!");
                        return;
                }
                if (plugin.getAuction() != null) {
                        plugin.warn(sender, "There is already an auction running!");
                        return;
                }
                Player player = (Player)sender;
                ItemStack lot = player.getItemInHand().clone();
                if(lot == null || lot.getType() == Material.AIR) {
                        plugin.warn(sender, "There is nothing in your hand!");
                        return;
                }
                // amount, optional
                if (args.hasNext()) {
                        String arg = args.next();
                        int amount = 0;
                        try {
                                amount = Integer.parseInt(arg);
                        } catch (NumberFormatException nfe) {
                                this.plugin.warn(sender, "Invalid amount argument: " + arg);
                                return;
                        }
                        if (amount < 1) {
                                plugin.warn(sender, "Invalid amount!");
                                return;
                        }
                        lot.setAmount(amount);
                }
                // price
                int price = plugin.getConfig().getInt("minincrement");
                if (args.hasNext()) {
                        String arg = args.next();
                        try {
                                price = Integer.parseInt(arg);
                        } catch (NumberFormatException nfe) {
                                this.plugin.warn(sender, "Invalid bid argument: " + arg);
                                return;
                        }
                        if (price < 0) {
                                plugin.warn(sender, "Invalid price!");
                                return;
                        }
                }
                if (args.hasNext()) {
                        plugin.warn(sender, "Too many arguments!");
                        return;
                }
                Auction auction = new Auction(plugin, lot, new PlayerMerchant(plugin, player), plugin.getConfig().getInt("auctiontime"), price);
                auction.start();
        }

        private void spam(CommandSender sender, Iterator<String> args) {
                if (!sender.hasPermission("auction.admin") && !sender.isOp()) {
                        plugin.warn(sender, "You don't have permission!");
                        return;
                }
                if (args.hasNext()) {
                        plugin.warn(sender, "Too many arguments!");
                        return;
                }
                Auction auction = plugin.getAuction();
                if (auction == null) {
                        plugin.warn(sender, "No auction running!");
                        return;
                }
                plugin.broadcast(auction.getInfoStrings());
        }

        private void help(CommandSender sender, Iterator<String> args) {
                if (args.hasNext()) {
                        plugin.warn(sender, "Too many arguments!");
                        return;
                }
                sender.sendMessage(col.AUX + "["
                                   + col.TITLE + "Auction Help"
                                   + col.AUX + "]");
                sender.sendMessage(col.HELP + "/auc "
                                   + col.HELPVALUE + "?"
                                   + col.AUX + " Display this message");
                sender.sendMessage(col.HELP + "/auc "
                                   + col.SHORTCUT + "b"
                                   + col.HELP + "id "
                                   + col.HELPVALUE + "[amount]"
                                   + col.AUX + " Place a bid");
                sender.sendMessage(col.HELP + "/bid "
                                   + col.HELPVALUE + "[amount]"
                                   + col.AUX + " Place a bid");
                sender.sendMessage(col.HELP + "/auc "
                                   + col.SHORTCUT + "s"
                                   + col.HELP + "tart "
                                   + col.HELPVALUE + "<item> <amount> [price]"
                                   + col.AUX + " Auction an item");
                sender.sendMessage(col.HELP + "/auc "
                                   + col.SHORTCUT + "h"
                                   + col.HELP + "and"
                                   + col.HELPVALUE + " [amount] [price]"
                                   + col.AUX + " Auction the item in your hand");
                sender.sendMessage(col.HELP + "/auc "
                                   + col.SHORTCUT + "e"
                                   + col.HELP + "nd "
                                   + col.HELPVALUE + "[delay]"
                                   + col.AUX + " End current auction");
                sender.sendMessage(col.HELP + "/auc "
                                   + col.SHORTCUT + "i"
                                   + col.HELP + "nfo"
                                   + col.AUX + " Display auction information"); 
                sender.sendMessage(col.HELP + "/auc "
                                   + col.HELPVALUE + "listen"
                                   + col.HELP + "|"
                                   + col.HELPVALUE + "ignore"
                                   + col.AUX + " Listen to or ignore auctions");
                if (sender.hasPermission("auction.admin") || sender.isOp()) {
                        sender.sendMessage(col.ADMIN + "/auc "
                                           + col.SHORTCUT + "c"
                                           + col.ADMIN + "ancel"
                                           + col.AUX + " Cancel an auction");
                        sender.sendMessage(col.ADMIN + "/auc bank "
                                           + col.VALUE + "<item> <amount> [bid] [time]"
                                           + col.AUX + " Auction spawned items");
                        sender.sendMessage(col.ADMIN + "/auc bank "
                                           + col.VALUE + "\"<name>\" [price] [time]"
                                           + col.AUX + " Auction a fake item");
                        sender.sendMessage(col.ADMIN + "/auc bankbid "
                                           + col.VALUE + "<amount>"
                                           + col.AUX + " Place a bid on behalf of the bank");
                        sender.sendMessage(col.ADMIN + "/auc spam"
                                           + col.AUX + " Broadcast a brief auction info");
                        sender.sendMessage(col.ADMIN + "/auc admininfo"
                                           + col.AUX + " Display confidential auction information");
                }
        }
}
