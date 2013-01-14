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

package edu.self.startux.craftBay.locale;

import edu.self.startux.craftBay.Auction;
import edu.self.startux.craftBay.AuctionTime;
import edu.self.startux.craftBay.CraftBayPlugin;
import edu.self.startux.craftBay.Item;
import edu.self.startux.craftBay.Merchant;
import edu.self.startux.craftBay.MoneyAmount;
import edu.self.startux.craftBay.event.AuctionBidEvent;
import edu.self.startux.craftBay.event.AuctionTimeChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.command.CommandSender;

public class Message {
        private static Object endl = new Object();
        private static class Variable {
                public String key;
        }
        private static class Escape {
                public String sequence;
                @Override public String toString() { return sequence; }
        }

        private LinkedList<Object> tokens = new LinkedList<Object>();
        private Map<String, Object> environment = new HashMap<String, Object>();

        public Message(List<String> input) {
                for (String inp : input) {
                        tokens.add(inp);
                        tokens.add(endl);
                }
                filterEscapes();
                filterColors();
                filterVariables();
        }

        public Message(String input) {
                tokens.add(input);
                tokens.add(endl);
                filterEscapes();
                filterColors();
                filterVariables();
        }

        /**
         * Empty message
         */
        public Message() {
        }

        public Message append(Message other) {
                tokens.addAll(other.tokens);
                environment.putAll(other.environment);
                return this;
        }

        /**
         * Copy constructor
         */
        public Message(Message other) {
                tokens = other.tokens;
                environment = new HashMap<String, Object>(other.environment);
        }

        private void filterEscapes() {
                ListIterator<Object> iter = tokens.listIterator();
                while (iter.hasNext()) {
                        Object o = iter.next();
                        if (!(o instanceof String)) continue;
                        iter.remove();
                        String string = (String)o;
                        Pattern pattern = Pattern.compile("\\\\(.)");
                        Matcher matcher = pattern.matcher(string);
                        int lastIndex = 0;
                        while (matcher.find()) {
                                iter.add(string.substring(lastIndex, matcher.start()));
                                iter.add(matcher.group(1));
                                lastIndex = matcher.end();
                        }
                        iter.add(string.substring(lastIndex, string.length()));
                }
        }

        private void filterColors() {
                ListIterator<Object> iter = tokens.listIterator();
                while (iter.hasNext()) {
                        Object o = iter.next();
                        if (!(o instanceof String)) continue;
                        iter.remove();
                        String string = (String)o;
                        Pattern pattern = Pattern.compile("<([^>]+)>");
                        Matcher matcher = pattern.matcher(string);
                        int lastIndex = 0;
                        while (matcher.find()) {
                                Color color = Color.getByName(matcher.group(1));
                                if (color == null) continue;
                                iter.add(string.substring(lastIndex, matcher.start()));
                                lastIndex = matcher.end();
                                iter.add(color);
                        }
                        iter.add(string.substring(lastIndex, string.length()));
                }
        }

        private void filterVariables() {
                ListIterator<Object> iter = tokens.listIterator();
                while (iter.hasNext()) {
                        Object o = iter.next();
                        if (!(o instanceof String)) continue;
                        iter.remove();
                        String string = (String)o;
                        Pattern pattern = Pattern.compile("\\{([^\\}]+)\\}");
                        Matcher matcher = pattern.matcher(string);
                        int lastIndex = 0;
                        while (matcher.find()) {
                                iter.add(string.substring(lastIndex, matcher.start()));
                                lastIndex = matcher.end();
                                Variable var = new Variable();
                                var.key = matcher.group(1);
                                environment.put(var.key, "{" + matcher.group(1) + "}");
                                iter.add(var);
                        }
                        iter.add(string.substring(lastIndex, string.length()));
                }
        }

        public List<String> compile() {
                List<String> result = new ArrayList<String>();
                StringBuilder sb = new StringBuilder();
                for (Object o : tokens) {
                        if (o == endl) {
                                result.add(sb.toString());
                                sb = new StringBuilder();
                        } else if (o instanceof Variable) {
                                sb.append(environment.get(((Variable)o).key).toString());
                        } else {
                                sb.append(o.toString());
                        }
                }
                return result;
        }

        public List<String> compileNoColor() {
                List<String> result = new ArrayList<String>();
                StringBuilder sb = new StringBuilder();
                for (Object o : tokens) {
                        if (o == endl) {
                                result.add(sb.toString());
                                sb = new StringBuilder();
                        } else if (o instanceof Variable) {
                                sb.append(environment.get(((Variable)o).key).toString());
                        } else if (o instanceof Color) {
                                // do nothing
                        } else {
                                sb.append(o.toString());
                        }
                }
                return result;
        }

        public Message set(String key, Object value) {
                environment.put(key, value);
                return this;
        }

        public Message set(Item item) {
                set("item", item.getName());
                set("itemdesc", item.getDescription());
                set("amount", item.getAmount());
                set("totalamount", item.getAmount().getInt());
                set("itemid", item.getId());
                set("itemdamage", item.getDamage());
                set("enchantments", item.getItemInfo());
                set("iteminfo", item.getItemInfo());
                return this;
        }

        public Message set(Auction auction) {
                set("id", auction.getId());
                set(auction.getItem());
                set("owner", auction.getOwner().getName());
                if (auction.getWinner() != null) set("winner", auction.getWinner().getName());
                set("minbid", auction.getMinimalBid());
                set("maxbid", auction.getMaxBid());
                set("price", auction.getWinningBid());
                set("fee", auction.getFee());
                set("state", auction.getState());
                set("timeleft", new AuctionTime(auction.getTimeLeft()));
                return this;
        }

        public Message set(CommandSender sender) {
                set("player", sender.getName());
                return this;
        }

        public Message set(Merchant merchant) {
                set("player", merchant.getName());
                return this;
        }

        public Message set(Auction auction, CommandSender sender) {
                set(auction);
                set(sender);
                return this;
        }

        public Message set(Auction auction, Merchant merchant) {
                set(auction);
                set(merchant);
                return this;
        }

        public Message set(AuctionBidEvent event) {
                set(event.getAuction());
                set(event.getBidder());
                set("bid", event.getAmount());
                if (event.getOldWinner() != null) set("oldwinner", event.getOldWinner().getName());
                set("oldprice", event.getOldPrice());
                return this;
        }

        public Message set(AuctionTimeChangeEvent event) {
                set(event.getAuction());
                set(event.getSender());
                set("newtimeleft", new AuctionTime(event.getDelay()));
                return this;
        }

        @Override
        protected Message clone() {
                return new Message(this);
        }

        @Override
        public String toString() {
                StringBuilder sb = new StringBuilder();
                for (String line : compileNoColor()) sb.append(line).append('\n');
                if (sb.length() > 0) sb.setLength(sb.length() - 1);
                return sb.toString();
        }
}
