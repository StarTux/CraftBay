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
import edu.self.startux.craftBay.AuctionTime;
import edu.self.startux.craftBay.CraftBayPlugin;
import edu.self.startux.craftBay.MoneyAmount;
import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AuctionParameters extends CommandParser {
        public AuctionParameters(CraftBayPlugin plugin) {
                super(plugin);
        }

        @Parameter
        public CommandSender commandSender(CommandSender sender) {
                return sender;
        }

        @Parameter
        public Player player(CommandSender sender) throws CommandParseException {
                if (!(sender instanceof Player)) {
                        throw new CommandParseException(plugin.getMessage("command.NotAPlayer"));
                }
                return (Player)sender;
        }
        
        @Parameter
        public Auction auction(CommandSender sender) throws CommandParseException {
                Auction auction = plugin.getAuction();
                if (auction == null) {
                        throw new CommandParseException(plugin.getMessage("command.NoCurrentAuction"));
                }
                return auction;
        }

        @Parameter
        public String string(CommandSender sender, String arg) {
                return arg;
        }

        @Parameter
        public Integer integer(CommandSender sender, String arg) throws CommandParseException {
                try {
                        return Integer.parseInt(arg);
                } catch (NumberFormatException e) {
                        throw new CommandParseException(plugin.getMessage("command.NotANumber").set("arg", arg));
                }
        }

        @Parameter
        public ItemStack itemStack(CommandSender sender, String arg) throws CommandParseException {
                ItemInfo item = Items.itemByString(arg);
                if (item == null) {
                        throw new CommandParseException(plugin.getMessage("command.NoSuchItem").set("arg", arg));
                }
                ItemStack stack = item.toStack();
                if (stack == null || stack.getType().equals(Material.AIR)) {
                        throw new CommandParseException(plugin.getMessage("command.IllegalItem").set("arg", arg));
                }
                return stack;
        }

        @Parameter
        public MoneyAmount moneyAmount(CommandSender sender, String arg) throws CommandParseException {
                double amount;
                try {
                        amount = Double.parseDouble(arg);
                } catch (NumberFormatException nfe) {
                        throw new CommandParseException(plugin.getMessage("command.NotANumber").set("arg", arg));
                }
                if (amount < 0.0) throw new CommandParseException(plugin.getMessage("command.NotANumber").set("arg", arg));
                return new MoneyAmount(amount);
        }

        @Parameter
        public AuctionTime auctionTime(CommandSender sender, String arg) throws CommandParseException {
                String[] tokens = arg.split(":", 2);
                int minutes = 0;
                int seconds = 0;
                try {
                        if (tokens.length >= 1) minutes = Integer.parseInt(tokens[0]);
                        if (tokens.length >= 2) seconds = Integer.parseInt(tokens[1]);
                        if (minutes < 0 || seconds < 0 || seconds >= 60) throw new CommandParseException(plugin.getMessage("command.BadTimeFormat").set("arg", arg));
                } catch (NumberFormatException nfe) {
                        throw new CommandParseException(plugin.getMessage("command.BadTimeFormat").set("arg", arg));
                }
                return new AuctionTime(minutes * 60 + seconds);
        }
}
