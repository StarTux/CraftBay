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

import edu.self.startux.craftBay.chat.ChatPlugin;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AuctionCommand implements CommandExecutor {
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.METHOD)
        private static @interface SubCommand {
                String name() default ""; // command name if it deviates from function name
                String[] aliases() default {}; // list of alternative names
                boolean shortcut() default false; // is there a one character shortcut?
                String perm() default ""; // permission node necessary to execute
                int optional() default 0; // count of optional args
        }
        private static class CommandAttribute {
                private AuctionCommand parent;
                private Method method;
                private String name;
                private List<String> aliases;
                private String permission = null;
                private boolean shortcut;
                private int optionalArgc = 0;

                public CommandAttribute(SubCommand subCommand, Method method) {
                        this.method = method;
                        if (subCommand.name().length() != 0) {
                                name = subCommand.name();
                        } else {
                                name = method.getName();
                        }
                        aliases = new ArrayList<String>();
                        for (String alias : subCommand.aliases()) aliases.add(alias);
                        shortcut = subCommand.shortcut();
                        if (shortcut) aliases.add("" + name.charAt(0));
                        if (subCommand.perm().length() != 0) {
                                permission = "auction." + subCommand.perm();
                        }
                        this.optionalArgc = subCommand.optional();
                }
                public boolean checkPermission(CommandSender sender) {
                        if (sender.isOp()) return true;
                        if (permission != null && !sender.hasPermission(permission)) return false;
                        return true;
                }
                public boolean call(AuctionCommand parent, CommandSender sender, Iterator<String> args) {
                        CraftBayPlugin plugin = CraftBayPlugin.getInstance();
                        Class<?>[] paramTypes = method.getParameterTypes();
                        Object[] params = new Object[paramTypes.length];
                        int avoided = 0;
                paramLoop:
                        for (int i = 0; i < params.length; ++i) {
                                String arg;
                                Class<?> paramType = paramTypes[i];
                                if (paramType.equals(CommandSender.class)) {
                                        params[i] = sender;
                                        continue paramLoop;
                                } else if (paramType.equals(Player.class)) {
                                        if (!(sender instanceof Player)) {
                                                parent.plugin.warn(sender, plugin.getLocale().getMessage("commands.parse.notaplayer").set(sender).compile());
                                                return false;
                                        }
                                        params[i] = (Player)sender;
                                        continue paramLoop;
                                } else if (paramType.equals(Auction.class)) {
                                        Auction auction = parent.plugin.getAuction();
                                        if (auction == null) {
                                                parent.plugin.warn(sender, plugin.getLocale().getMessage("commands.parse.nocurrentauction").set(sender).compile());
                                                return false;
                                        }
                                        params[i] = auction;
                                        continue paramLoop;
                                }
                                if (!args.hasNext()) {
                                        avoided += 1;
                                        if (avoided > optionalArgc) {
                                                parent.plugin.warn(sender, plugin.getLocale().getMessage("commands.parse.argstoosmall").set(sender).compile());
                                                return false;
                                        }
                                        arg = null;
                                } else {
                                        arg = args.next();
                                }
                                if (paramType.equals(String.class)) {
                                        params[i] = arg;
                                } else if (paramType.equals(Integer.class)) {
                                        if (arg == null) {
                                                params[i] = null;
                                                continue paramLoop;
                                        }
                                        try {
                                                params[i] = Integer.parseInt(arg);
                                        } catch (NumberFormatException e) {
                                                parent.plugin.warn(sender, plugin.getLocale().getMessage("commands.parse.nonumber").set(sender).set("arg", arg).compile());
                                                return false;
                                        }
                                } else if (paramType.equals(int.class)) {
                                        if (arg == null) {
                                                params[i] = 0;
                                                continue paramLoop;
                                        }
                                        try {
                                                params[i] = Integer.parseInt(arg);
                                        } catch (NumberFormatException e) {
                                                parent.plugin.warn(sender, plugin.getLocale().getMessage("commands.parse.nonumber").set(sender).set("arg", arg).compile());
                                                return false;
                                        }
                                } else if (paramType.equals(ItemStack.class)) {
                                        if (arg == null) {
                                                params[i] = null;
                                                continue paramLoop;
                                        }
                                        ItemInfo item = Items.itemByString(arg);
                                        if (item == null) {
                                                parent.plugin.warn(sender, plugin.getLocale().getMessage("commands.parse.nosuchitem").set(sender).set("arg", arg).compile());
                                                return false;
                                        }
                                        ItemStack stack = item.toStack();
                                        if (stack == null || stack.getType().equals(Material.AIR)) {
                                                parent.plugin.warn(sender, plugin.getLocale().getMessage("commands.parse.illegalitem").set(sender).set("arg", arg).compile());
                                                return false;
                                        }
                                        params[i] = stack;
                                }
                        }
                        if (args.hasNext()) {
                                parent.plugin.warn(sender, plugin.getLocale().getMessage("commands.parse.argstoobig").set(sender).compile());
                                return false;
                        }
                        try {
                                method.invoke(parent, params);
                        } catch (Exception e) {
                                System.err.println(e);
                                e.printStackTrace();
                                return false;
                        }
                        return true;
                }
        }

        private CraftBayPlugin plugin;
        private Map<String, CommandAttribute> commandMap = new HashMap<String, CommandAttribute>();

	AuctionCommand(CraftBayPlugin plugin) {
		this.plugin = plugin;
                for (Method method : getClass().getMethods()) {
                        if (method.isAnnotationPresent(SubCommand.class)) {
                                SubCommand subCommand = method.getAnnotation(SubCommand.class);
                                CommandAttribute attr = new CommandAttribute(subCommand, method);
                                commandMap.put(attr.name.toLowerCase(), attr);
                                for (String alias : attr.aliases) {
                                        commandMap.put(alias.toLowerCase(), attr);
                                }
                        }
                }
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] argv) {
                Iterator<String> args = Arrays.asList(argv).iterator();
                String arg;
                if (label.equals("bid")) {
                        arg = "bid";
                } else {
                        if (!args.hasNext()) {
                                Auction auction = plugin.getAuction();
                                if (auction != null) {
                                        info(sender, auction);
                                } else {
                                        help(sender);
                                }
                                return true;
                        }
                        arg = args.next();
                }
                CommandAttribute attr = commandMap.get(arg.toLowerCase());
                if (attr == null) {
                        plugin.warn(sender, plugin.getLocale().getMessage("command.noentry").set("command", arg).compile());
                        return true;
                }
                if (!attr.checkPermission(sender)) {
                        plugin.warn(sender, plugin.getLocale().getMessage("command.noentry").set("command", arg).compile());
                        return true;
                }
                try {
                        attr.call(this, sender, args);
                } catch (Exception e) {
                        System.err.println(e.getMessage());
                        e.printStackTrace(System.err);
                        return true;
                }
                return true;
        }

        @SubCommand(perm = "info", shortcut = true)
        public void info(CommandSender sender, Auction auction) {
                plugin.msg(sender, plugin.getLocale().getMessages("auction.info.head", "auction.info.owner", "auction.info.item", (auction.getWinner() != null ? "auction.info.winner" : "auction.info.nowinner"), (auction.getState() != AuctionState.ENDED ? "auction.info.time" : "auction.info.state"), "auction.info.help").set(auction, sender).compile());
        }

        @SubCommand(perm = "bid", shortcut = true, optional = 1)
        public void bid(Player player, Auction auction, Integer amount) {
                if (amount == null) amount = auction.getMinimalBid();
                auction.bid(new PlayerMerchant(player), amount);
        }
        
        @SubCommand(perm = "start", shortcut = true, optional = 1)
        public void end(CommandSender sender, Auction auction, Integer delay) {
                if (!auction.getOwner().equals(sender)) {
                        if (!sender.hasPermission("auction.admin") && !sender.isOp()) {
                                plugin.warn(sender, plugin.getLocale().getMessage("auction.end.notowner").set(auction, sender).compile());
                                return;
                        }
                }
                if (delay == null) {
                        auction.stop();
                        plugin.getAuctionScheduler().soon();
                } else {
                        if (delay > auction.getTimeLeft()) {
                                if (!sender.hasPermission("auction.admin") && !sender.isOp()) {
                                        plugin.warn(sender, plugin.getLocale().getMessage("auction.end.delaytoolong").set(auction, sender).compile());
                                        return;
                                }
                        }
                        auction.setTimeLeft(delay);
                }
        }

        @SubCommand(perm = "admin", shortcut = true)
        public void cancel(CommandSender sender, Auction auction) {
                auction.cancel();
                plugin.getAuctionScheduler().soon();
                plugin.broadcast(plugin.getLocale().getMessage("auction.announce.cancel").set(auction, sender).compile());
        }

        @SubCommand(perm = "admin")
        public void bankBid(CommandSender sender, Auction auction, Integer amount) {
                if (amount == null) amount = auction.getMinimalBid();
                auction.bid(BankMerchant.getInstance(), amount);
        }
        
        @SubCommand(perm = "info")
        public void listen(Player player) {
                ChatPlugin chatPlugin = plugin.getChatPlugin();
                if (chatPlugin.isListening(player)) {
                        plugin.msg(player, plugin.getLocale().getMessage("commands.listen.alreadylisten").set(player).compile());
                        return;
                }
                boolean ret = plugin.getChatPlugin().listen(player, true);
                if (!ret) {
                        plugin.msg(player, plugin.getLocale().getMessage("commands.listen.listenerror").set(player).compile());
                        return;
                }
                plugin.msg(player, plugin.getLocale().getMessage("commands.listen.listensuccess").set(player).compile());
        }

        @SubCommand(perm = "info")
        public void ignore(Player player) {
                ChatPlugin chatPlugin = plugin.getChatPlugin();
                if (!chatPlugin.isListening(player)) {
                        plugin.msg(player, plugin.getLocale().getMessage("commands.listen.alreadyignore").set(player).compile());
                        return;
                }
                boolean ret = plugin.getChatPlugin().listen(player, false);
                if (!ret) {
                        plugin.msg(player, plugin.getLocale().getMessage("commands.listen.ignoreerror").set(player).compile());
                        return;
                }
                plugin.msg(player, plugin.getLocale().getMessage("commands.listen.ignoresuccess").set(player).compile());
        }

        @SubCommand(perm = "start", shortcut = true, optional = 2)
        public void start(Player player, ItemStack stack, Integer amount, Integer price) {
                if (amount == null) amount = 1;
                stack.setAmount(amount);
                Merchant merchant = new PlayerMerchant(player);
                Item item = new RealItem(stack);
                Auction auction = plugin.getAuctionHouse().createAuction(merchant, item);
                if (auction == null) return;
                if (price != null) auction.setStartingBid(price);
                plugin.getAuctionScheduler().queueAuction(auction);
        }

        @SubCommand(perm = "admin", optional = 3)
        public void bank(CommandSender sender, ItemStack stack, Integer amount, Integer price, Integer time) {
                if (amount == null) amount = 1;
                stack.setAmount(amount);
                Merchant merchant = BankMerchant.getInstance();
                RealItem item = new RealItem(stack);
                Auction auction = plugin.getAuctionHouse().createAuction(merchant, item);
                if (auction == null) return;
                if (time != null) auction.setTimeLeft(time);
                if (price != null) auction.setStartingBid(price);
                plugin.getAuctionScheduler().queueAuction(auction);
        }

        @SubCommand(perm = "start", optional = 2)
        public void hand(Player player, Integer amount, Integer price) {
                HandItem item;
                try {
                        item = new HandItem(player);
                } catch (IllegalArgumentException iae) {
                        plugin.warn(player, "There is nothing in your hand!");
                        return;
                }
                if (amount != null) item.setAmount(amount);
                Merchant merchant = new PlayerMerchant(player);
                Auction auction = plugin.getAuctionHouse().createAuction(merchant, item);
                if (auction == null) return;
                if (price != null) auction.setStartingBid(price);
                plugin.getAuctionScheduler().queueAuction(auction);
                plugin.msg(player, "success!");
        }

        @SubCommand(perm = "info", optional = 1, aliases = { "hist" })
        public void history(CommandSender sender, Integer id) {
                List<String> msg = new ArrayList<String>();
                msg.addAll(plugin.getLocale().getMessage("history.head").set(sender).compile());
                if (id == null) {
                        for (Auction auction : plugin.getAuctionScheduler().getQueue()) {
                                msg.addAll(plugin.getLocale().getMessage("history.queue").set(auction).compile());
                        }
                        {
                                Auction auction = plugin.getAuctionScheduler().getCurrentAuction();
                                if (auction != null) {
                                        msg.addAll(plugin.getLocale().getMessage("history.current").set(auction).compile());
                                }
                        }
                        for (Auction auction : plugin.getAuctionScheduler().getHistory()) {
                                msg.addAll(plugin.getLocale().getMessage("history.history").set(auction).compile());
                        }
                        plugin.msg(sender, msg);
                        return;
                }
                Auction auction = plugin.getAuctionScheduler().getById(id);
                if (auction == null) {
                        plugin.warn(sender, plugin.getLocale().getMessage("commands.history.noentry").set(sender).set("auctionid", id).compile());
                        return;
                }
                info(sender, auction);
        }

        @SubCommand(perm = "admin")
        public void spam(CommandSender sender, Auction auction) {
        }

        @SubCommand(name = "?", aliases = { "h", "help" })
        public void help(CommandSender sender) {
                List<String> msg = plugin.getLocale().getMessage("help").compile();
                if (sender.hasPermission("auction.admin") || sender.isOp()) {
                        msg.addAll(plugin.getLocale().getMessage("adminhelp").compile());
                }
                plugin.msg(sender, msg);
        }
}
