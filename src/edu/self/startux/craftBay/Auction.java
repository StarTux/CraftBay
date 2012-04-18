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

import java.util.Arrays;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.milkbowl.vault.item.Items;
import net.milkbowl.vault.item.ItemInfo;
import org.bukkit.enchantments.Enchantment;

public class Auction implements Runnable {
        private static String romans[] = {"", "I", "II", "III", "IV", "V"};

	private int time;
	private ItemStack lot;
	private Merchant owner;
        private int minbid;
        private int spamInterval = 120;

	private int timeLeft;
	private CraftBayPlugin plugin;
        private String comment;

	private int task = 0;
        private int id = 0;
        private static int nextId = 0;

        private AuctionColors col = new AuctionColors();

        private List<Bid> bids = new ArrayList<Bid>();
        private int bidCount = 0;

	public Auction(CraftBayPlugin plugin, ItemStack lot, Merchant owner, int time, int minbid) {
                this.plugin = plugin;
                this.lot = lot;
                this.owner = owner;
                this.time = time;
                this.minbid = minbid;
                comment = null;
		timeLeft = time;
                id = nextId++;
                spamInterval = plugin.getConfig().getInt("spaminterval");
                // plugin.log("[" + id + "] CREATE owner(" + owner.getName() + ")");
	}

        public Merchant getOwner() {
                return owner;
        }

        public void start() {
                if (!owner.hasItem(lot)) {
                        owner.warn("You don't have enough of that item!");
                        return;
                }
                owner.takeItem(lot);
                plugin.setAuction(this);
                plugin.broadcast(getInfoStrings());
                task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 0l, 20l);
                plugin.log("[" + id + "] START owner(" + owner.getName() + "), item(" + getItemDescription() + ")");
        }
        
        public int getTimeLeft() {
                return timeLeft;
        }

        public void setTimeLeft(int time) {
                timeLeft = time;
                if (time > this.time) {
                        this.time = time;
                }
                plugin.broadcast(getShortInfoStrings().get(0));
        }

        public List<String> getShortInfoStrings() {
                Merchant winner = getWinner();
                List<String> list = new ArrayList<String>(2);
                if (winner != null) {
                        list.add(col.VALUE + winner.getName()
                                 + col.FIELD + " wins "
                                 + col.VALUE + getItemAmount()
                                 + col.FIELD + "x"
                                 + col.VALUE + getItemName()
                                 + col.FIELD + " in "
                                 + col.VALUE + timeLeft
                                 + col.FIELD + " seconds.");
                        list.add(col.FIELD + "Type at least "
                                 + col.VALUE + "/bid " + getMinimalBid()
                                 + col.FIELD + " to compete!");
                } else {
                        list.add( col.FIELD + "Auction for "
                                  + col.VALUE + getItemAmount()
                                  + col.FIELD + "x"
                                  + col.VALUE + getItemName()
                                  + col.FIELD + " ends in "
                                  + col.VALUE + timeLeft
                                  + col.FIELD + " seconds.");
                        list.add(col.FIELD + "Type at least "
                                 + col.VALUE + "/bid " + getMinimalBid()
                                 + col.FIELD + " to win!");
                }
                return list;
        }

	@Override
	public void run() {
                if (timeLeft == 0) {
			stop();
                        return;
		} else if (timeLeft % spamInterval == 0 || timeLeft == 10) {
                        if (timeLeft != time) { // avoid spamming both big and small info at start
                                plugin.broadcast(getShortInfoStrings());
                        }
                } else if (timeLeft <= 3) {
                        plugin.broadcast(getShortInfoStrings().get(0));
                }
                timeLeft -= 1;
        }

        protected void end() {
                Merchant winner = getWinner();
		if (winner == null) {
                        owner.giveItem(lot);
                        owner.msg(col.FIELD + "Your "
                                  + col.VALUE + getItemName()
                                  + col.FIELD + " has been returned to you.");
			plugin.broadcast(col.FIELD + "Auction for "
                                         + col.VALUE + getItemAmount()
                                         + col.FIELD + "x"
                                         + col.VALUE + getItemName()
                                         + col.FIELD + " ended without any bids.");
                        plugin.log("[" + id + "] END return(no bids)");
                        return;
                }
                if (!winner.hasAmount(getEffectiveBid())) {
                        owner.giveItem(lot);
                        owner.msg("Error processing payment. Auction canceled.");
                        winner.msg("Error processing payment. Auction canceled.");
                        plugin.log("[" + id + "] END return(payment error) winner(" + winner.getName() + "), price(" + getEffectiveBid() + ")");
                        return;
                }
                winner.takeAmount(getEffectiveBid());
                owner.giveAmount(getEffectiveBid());
                winner.giveItem(lot);
                plugin.broadcast(col.VALUE + winner.getName()
                                 + col.FIELD + " buys "
                                 + col.VALUE + getItemAmount()
                                 + col.FIELD + "x"
                                 + col.VALUE + getItemName()
                                 + col.FIELD + " for "
                                 + col.VALUE + plugin.getEco().format(getEffectiveBid())
                                 + col.FIELD + ".");
                winner.msg(col.FIELD + "Enjoy your "
                           + col.VALUE + getItemAmount()
                           + col.FIELD + "x"
                           + col.VALUE + getItemName()
                           + col.FIELD + " for "
                           + col.VALUE + plugin.getEco().format(getEffectiveBid())
                           + col.FIELD + "!");
                owner.msg(col.FIELD + "Your "
                          + col.VALUE + getItemAmount()
                          + col.FIELD + "x"
                          + col.VALUE + getItemName()
                          + col.FIELD + " has been sold for "
                          + col.VALUE + plugin.getEco().format(getEffectiveBid())
                          + col.FIELD + ".");
                plugin.log("[" + id + "] END winner(" + winner.getName() + "), price(" + getEffectiveBid() + ")");
                return;
        }

	public void stop() {
                end();
                plugin.getServer().getScheduler().cancelTask(task);
                task = 0;
                plugin.setAuction(null);
        }

        public void cancel(CommandSender sender) {
                plugin.broadcast(col.ADMIN + "Auction canceled by " + col.ADMINHI + sender.getName() + col.ADMIN + "!");
                owner.warn("Your auction has been canceled!");
                owner.giveItem(lot);
                plugin.getServer().getScheduler().cancelTask(task);
                task = 0;
                plugin.setAuction(null);
                plugin.log("[" + id + "] CANCEL");
	}

        public Merchant getWinner() {
                if (bids.isEmpty()) {
                        return null;
                }
                return bids.get(bids.size() - 1).getBidder();
        }

        /**
         *
         */
        public int getMinIncrement() {
                int min = plugin.getConfig().getInt("minincrement");
                if (min < 1) min = 1;
                return min;
        }

        public int getBid(Merchant merchant) throws IllegalArgumentException {
                for (Bid bid : bids) {
                        if (merchant.equals(bid.getBidder())) {
                                return bid.getAmount();
                        }
                }
                throw new IllegalArgumentException("Merchant `" + merchant.getName() + "' never placed a bid");
        }

        /**
         * Get the highest bid so far.
         */
        public int getMaxBid() {
                if (bids.isEmpty()) {
                        return minbid;
                }
                return bids.get(bids.size() - 1).getAmount();
        }

        /**
         * Get the amount that the current winner would have to
         * pay, should he win.
         */
        public int getEffectiveBid() {
                if (bids.isEmpty()) return 0;
                if (bids.size() == 1) return minbid;
                return Math.min(bids.get(bids.size() - 2).getAmount() + getMinIncrement(),
                                bids.get(bids.size() - 1).getAmount());
        }

        /**
         * Get the minimum amount that a new bidder has to make to
         * be accepted.
         */
        public int getMinimalBid() {
                if (bids.isEmpty()) return minbid;
                if (bids.size() == 1) return minbid + getMinIncrement();
                return Math.min(bids.get(bids.size() - 2).getAmount() + (getMinIncrement() * 2),
                                bids.get(bids.size() - 1).getAmount() + getMinIncrement());
        }

        /**
         * Insert a new bid and assert descending order of bids by
         * amount. Assume validity of bid.
         */
        public void addBid(Bid bid) {
                boolean found = false;
                for (Bid existing : bids) {
                        if (bid.getBidder().equals(existing.getBidder())) {
                                existing.setAmount(bid.getAmount());
                                existing.setCount(bidCount++);
                                found = true;
                                break;
                        }
                }
                if (!found) {
                        bids.add(bid);
                        bid.setCount(bidCount++);
                }
                Bid[] bidArray = bids.toArray(new Bid[0]);
                Arrays.sort(bidArray);
                bids.clear();
                for (Bid cpy : bidArray) {
                        bids.add(cpy);
                }
                // if (bids.isEmpty()) { // first bid
                //         bids.add(bid);
                // } else if (bid.getAmount() > bids.getFirst().getAmount()) { // new top bid
                //         // if you overbid yourself, only your bed gets updated
                //         if (bid.getBidder().equals(bids.getFirst().getBidder())) {
                //                 bids.set(0, bid);
                //         } else {
                //                 effectiveBid = bids.getFirst().getAmount() + getMinIncrement();
                //                 bids.addFirst(bid);
                //                 if (bids.size() > 2) {
                //                         bids.removeLast();
                //                 }
                //         }
                // } else { // 2nd most effective bid
                //         effectiveBid = bid.getAmount();
                //         if (bids.size() == 2) {
                //                 bids.set(1, bid);
                //         } else {
                //                 bids.addLast(bid);
                //         }
                // }
        }
        
	public boolean bid(Merchant bidder, int bid) {
		if (bidder.equals(owner) && bidder != BankMerchant.getInstance()) {
			bidder.warn("You cannot bid on your own auction!");
			return false;
		}
		if (bidder.equals(getWinner()) && bid <= getMaxBid() && bidder != BankMerchant.getInstance()) {
		 	bidder.warn("You have already made a larger bid");
		 	return false;
		}
                if (bid < getMinimalBid()) {
                        bidder.warn("Your must bid at least " + plugin.getEco().format(getMinimalBid()));
                        return false;
                }
                if (!bidder.hasAmount(bid)) {
                        bidder.warn("You do not have enough money");
                        return false;
                }
                bidder.msg(col.FIELD + "Your bid for "
                           + col.VALUE + plugin.getEco().format(bid)
                           + col.FIELD + " has been accepted.");
                int oldAmount = getEffectiveBid();
                Merchant oldWinner = getWinner();
                addBid(new Bid(bidder, bid));
                int newAmount = getEffectiveBid();
                Merchant newWinner = getWinner();
                if (oldWinner == null || (newAmount != oldAmount && oldWinner.equals(bidder))) {
                        // overbid, first winner or overbid yourself with price change (corner case)
                        plugin.broadcast(col.FIELD + "Price for "
                                         + col.VALUE + getItemAmount()
                                         + col.FIELD + "x"
                                         + col.VALUE + getItemName()
                                         + col.FIELD + " is now "
                                         + col.VALUE + plugin.getEco().format(newAmount)
                                         + col.FIELD + " for "
                                         + col.VALUE + newWinner.getName());
                } else if (!newWinner.equals(oldWinner)) {
                        // new beats old
                        plugin.broadcast(col.VALUE + bidder.getName()
                                         + col.FIELD + " beats "
                                         + col.FIELD + oldWinner.getName()
                                         + col.FIELD + ". Price "
                                         + col.VALUE + plugin.getEco().format(newAmount));
                } else if (newAmount != oldAmount) {
                        // underbid
                        plugin.broadcast(col.FIELD + bidder.getName()
                                         + col.FIELD + " raises but "
                                         + col.VALUE + oldWinner.getName()
                                         + col.FIELD + " is higher. Price "
                                         + col.VALUE + plugin.getEco().format(newAmount));
                }
                return true;
        }

        public int getItemAmount() {
                if (lot == null) {
                        return 1;
                }
                return lot.getAmount();
        }

        public String getItemName() {
                if (lot == null) {
                        return comment;
                }
                try {
                        return Items.itemByStack(lot).getName();
                } catch (NullPointerException npe) {
                        // I had outdated versions of vault fail for some reason
                        return lot.getType().name();
                }
        }

        private String getEnchantmentName(Enchantment enchantment) {
                switch(enchantment.getId()) {
                case 0: return "Protection";
                case 1: return "Fire Protection";
                case 2: return "Feather Falling";
                case 3: return "Blast Protection";
                case 4: return "Projectile Protection";
                case 5: return "Respiration";
                case 6: return "Aqua Affinity";
                case 16: return "Sharpness";
                case 17: return "Smite";
                case 18: return "Bane of Arthropods";
                case 19: return "Knockback";
                case 20: return "Fire Aspect";
                case 21: return "Looting";
                case 48: return "Power";
                case 49: return "Punch";
                case 50: return "Flame";
                case 51: return "Infinity";
                case 32: return "Efficiency";
                case 33: return "Silk Touch";
                case 34: return "Unbreaking";
                case 35: return "Fortune";
                default: return "UNKNOWN";
                }
        }

        public String getItemFormat() {
                if (lot == null) {
                        return col.VALUE + getItemName();
                }
                String name = "";
                if (getItemAmount() > 1) {
                        name += "" + col.VALUE + getItemAmount()
                                + col.FIELD + "x";
                }
                name += col.VALUE + getItemName()
                        + col.FIELD + " [" + col.VALUE
                        + lot.getTypeId() + ":" + lot.getDurability() + col.FIELD + "]";
                Map<Enchantment, Integer> enchantments = lot.getEnchantments();
                if (!enchantments.isEmpty()) {
                        boolean comma = false;
                        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                                if (comma) {
                                        name += col.FIELD + ", ";
                                } else {
                                        name += " ";
                                        comma = true;
                                }
                                Enchantment enchantment = entry.getKey();
                                int level = entry.getValue();
                                name += col.VALUE + getEnchantmentName(enchantment) + " " + roman(level);
                        }
                }
                return name;
        }

        /**
         * Unformatted item description
         */
        public String getItemDescription() {
                if (lot == null) {
                        return comment;
                }
                String name = "";
                name += "" + lot.getTypeId() + ":" + lot.getDurability() + " " + lot.getAmount() + ", " + lot.getType().name();
                Map<Enchantment, Integer> enchantments = lot.getEnchantments();
                if (!enchantments.isEmpty()) {
                        boolean comma = false;
                        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                                if (comma) {
                                        name += ":";
                                } else {
                                        name += " ";
                                        comma = true;
                                }
                                Enchantment enchantment = entry.getKey();
                                int level = entry.getValue();
                                name += enchantment.getName() + level;
                        }
                }
                return name;
        }

        /**
         * Return a list of lines with formatted information about the current auction.
         */
        public List<String> getInfoStrings() {
                List<String> lines = new ArrayList<String>(10);
		lines.add(col.AUX + "[" + col.TITLE + "Auction Information" + col.AUX + "]");
                lines.add(col.FIELD + "Auctioneer: " + col.VALUE + owner.getName());
                lines.add(col.FIELD + "Item: " + getItemFormat());
                Merchant winner = getWinner();
                if (winner == null) {
                        lines.add(col.FIELD + "Minimal bid: " + col.VALUE + plugin.getEco().format(getMinimalBid()));
                } else {
                        lines.add(col.FIELD + "Winning: "
                                  + col.VALUE + winner.getName()
                                  + col.FIELD + " for "
                                  + col.VALUE + plugin.getEco().format(getEffectiveBid()));
                }
                lines.add(col.FIELD + "Time left: " + col.VALUE + timeLeft + " seconds");
                lines.add(col.FIELD + "Type "
                          + col.VALUE + "/auc ?"
                          + col.FIELD + " for a list of commands.");
                return lines;
        }

        public void setComment(String comment) {
                this.comment = comment;
        }

	public void info(CommandSender sender) {
                List<String> lines = getInfoStrings();
                if (sender instanceof Player) {
                        try {
                                lines.add(col.FIELD + "Your bid: "
                                          + col.VALUE + plugin.getEco().format(getBid(new PlayerMerchant(plugin, (Player)sender))));
                        } catch (IllegalArgumentException iae) {
                                // do nothing
                        }
                }
                plugin.msg(sender, lines);
	}

        private String roman(int i) {
                try {
                        return romans[i];
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                        return "" + i;
                }
        }

        public void adminInfo(CommandSender sender) {
                List<String> list = new ArrayList<String>(bids.size() + 1);
                list.add(col.TITLE + "[Confidential Auction Information]");
                for (int i = bids.size(); i > 0; --i) {
                        Bid bid = bids.get(i - 1);
                        list.add(col.ADMIN + bid.getBidder().getName()
                                 + col.ADMINHI + " " + plugin.getEco().format(bid.getAmount()));
                }
                plugin.msg(sender, list);
        }
}