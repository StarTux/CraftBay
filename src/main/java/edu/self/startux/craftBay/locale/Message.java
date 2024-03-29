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
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class Message {
    private static Object endl = new Object();

    @Value
    private static final class Variable {
        private final String key;
        private final Style style;
    }

    private static final class Escape {
        private String sequence;
        @Override
        public String toString() {
            return sequence;
        }
    }

    private List<Object> tokens = new ArrayList<Object>();
    private Map<String, Object> environment = new HashMap<String, Object>();

    public Message(final List<String> input) {
        if (!input.isEmpty()) {
            tokens.add(input.get(0));
            for (int i = 1; i < input.size(); i += 1) {
                tokens.add(endl);
                tokens.add(input.get(i));
            }
        }
        filterEscapes();
        filterColors();
        filterVariables();
    }

    public Message(final String input) {
        tokens.add(input);
        filterEscapes();
        filterColors();
        filterVariables();
    }

    /**
     * Copy constructor.
     */
    public Message(final Message other) {
        tokens = other.tokens;
        environment = new HashMap<String, Object>(other.environment);
    }

    /**
     * Empty message.
     */
    public Message() { }

    public Message append(Message other) {
        if (other.tokens.isEmpty()) return this;
        if (!tokens.isEmpty()) {
            tokens.add(endl);
        }
        tokens.addAll(other.tokens);
        environment.putAll(other.environment);
        return this;
    }

    private void filterEscapes() {
        ListIterator<Object> iter = tokens.listIterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (!(o instanceof String)) continue;
            iter.remove();
            String string = (String) o;
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
            String string = ChatColor.translateAlternateColorCodes('&', (String) o);
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
            String string = (String) o;
            Pattern pattern = Pattern.compile("\\{([^\\}]+)\\}");
            Matcher matcher = pattern.matcher(string);
            int lastIndex = 0;
            while (matcher.find()) {
                iter.add(string.substring(lastIndex, matcher.start()));
                lastIndex = matcher.end();
                String name = matcher.group(1);
                Style.Builder style = Style.style();
                String[] split = name.split("#");
                if (split.length > 1) {
                    name = split[0];
                    for (int i = 1; i < split.length; i += 1) {
                        String it = split[i];
                        TextDecoration td = TextDecoration.NAMES.value(it);
                        if (td != null) {
                            style.decorate(td);
                            continue;
                        }
                        try {
                            TextColor textColor = TextColor.fromHexString("#" + it);
                            style.color(textColor);
                        } catch (IllegalArgumentException iae) {
                            CraftBayPlugin.getInstance().getLogger().warning("Invalid color code: " + it);
                        }
                    }
                }
                while (name.length() >= 2) {
                    if (name.charAt(0) != ChatColor.COLOR_CHAR) break;
                    ChatColor chatColor = ChatColor.getByChar(name.charAt(1));
                    if (chatColor == null) break;
                    name = name.substring(2);
                    if (chatColor.isColor()) {
                        style.color(NamedTextColor.NAMES.value(chatColor.name().toLowerCase()));
                    } else if (chatColor.isFormat()) {
                        style.decorate(TextDecoration.NAMES.value(chatColor.name().toLowerCase()));
                    }
                }
                environment.put(name, "{" + name + "}");
                Variable variable = new Variable(name, style.build());
                iter.add(variable);
            }
            iter.add(string.substring(lastIndex, string.length()));
        }
    }

    public Component compile() {
        TextComponent.Builder cb = Component.text();
        for (Object o : tokens) {
            if (o == endl) {
                cb.append(Component.newline());
            } else if (o instanceof Variable) {
                Variable variable = (Variable) o;
                Object object = environment.get(variable.key);
                if (object instanceof Component) {
                    Component component = (Component) object;
                    component = component.style(variable.style.merge(component.style()));
                    cb.append(component);
                } else if (object instanceof MoneyAmount) {
                    MoneyAmount moneyAmount = (MoneyAmount) object;
                    Component component = moneyAmount.toComponent();
                    component = component.style(variable.style.merge(component.style()));
                    cb.append(component);
                } else {
                    String string = object != null ? object.toString() : "";
                    cb.append(Component.text(string, variable.style));
                }
            } else {
                cb.append(Component.text(o.toString()));
            }
        }
        return cb.build();
    }

    public Message set(String key, Object value) {
        environment.put(key, value);
        return this;
    }

    public Message set(Item item) {
        set("item", item.toComponent());
        set("itemdesc", item.getDescription());
        set("amount", item.getAmount());
        set("totalamount", item.getAmount().getInt());
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
        set("startingbid", new MoneyAmount(CraftBayPlugin.getInstance().getConfig().getDouble("startingbid")));
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
        return PlainTextComponentSerializer.plainText().serialize(compile());
    }
}
