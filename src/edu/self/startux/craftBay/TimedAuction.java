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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TimedAuction extends AbstractAuction implements Runnable {
	private CraftBayPlugin plugin;
	private int timeLeft;
        private Item item;
	private Merchant owner;
        private int minbid;
        private int spamInterval = 120;
        private int minIncrement = 5;
	private int taskid = -1;
        private int id = 0;
        private static int nextId = 0;
        private LinkedList<Bid> bids = new LinkedList<Bid>();

	public TimedAuction(CraftBayPlugin plugin, Merchant owner, Item item) {
                this.plugin = plugin;
                this.owner = owner;
                this.item = item;
                minbid = plugin.getConfig().getInt("minincrement");
                timeLeft = plugin.getConfig().getInt("auctiontime");
                id = nextId++;
                spamInterval = plugin.getConfig().getInt("spaminterval");
                minIncrement = plugin.getConfig().getInt("minincrement");
                state = AuctionState.RUNNING;
	}

        @Override
        public int getId() {
                return id;
        }

        @Override
        public AuctionState getState() {
                return state;
        }

        @Override
        public Item getItem() {
                return item;
        }

        @Override
        public Merchant getOwner() {
                return owner;
        }

        @Override
        public void start() {
                taskid = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 0l, 20l);
                if (taskid == -1) {
                        plugin.log("TimedAuction failed scheduleSyncRepeatingTask()", Level.SEVERE);
                }
                plugin.getChatPlugin().broadcast(plugin.getLocale().getMessages("auction.info.head", "auction.info.owner", "auction.info.item", "auction.info.nowinner", "auction.info.time", "auction.info.help").set(this).compile());
        }

        @Override
        public int getTimeLeft() {
                return timeLeft;
        }

        @Override
        public void setTimeLeft(int time) {
                timeLeft = time;
        }

        @Override
        public int getStartingBid() {
                return minbid;
        }

        @Override
        public void setStartingBid(int amount) {
                minbid = amount;
        }

	@Override
	public void run() {
                if (timeLeft == 0) {
			stop();
                        plugin.getAuctionScheduler().soon();
                        return;
                }
                timeLeft -= 1;
        }

        private void end() {
                Merchant winner = getWinner();
		if (winner == null) {
                        ItemDelivery.schedule(owner, item, this);
                        owner.msg(plugin.getLocale().getMessage("auction.end.ownerreturn").set(this).compile());
			plugin.broadcast(plugin.getLocale().getMessage("auction.announce.nobid").set(this).compile());
                        plugin.log("[" + id + "] END return(no bids)");
                        return;
                }
                if (!winner.hasAmount(getEffectiveBid())) {
                        ItemDelivery.schedule(owner, item, this);
                        owner.msg(plugin.getLocale().getMessage("auction.end.ownerpaymenterror").set(this).compile());
                        winner.msg(plugin.getLocale().getMessage("auction.end.winnerpaymenterror").set(this).compile());
                        plugin.log("[" + id + "] END return(payment error) winner(" + winner.getName() + "), price(" + getEffectiveBid() + ")");
                        return;
                }
                winner.takeAmount(getEffectiveBid());
                owner.giveAmount(getEffectiveBid());
                ItemDelivery.schedule(winner, item, this);
                plugin.broadcast(plugin.getLocale().getMessage("auction.announce.nobid").set(this).compile());
                winner.msg(plugin.getLocale().getMessage("auction.end.winner").set(this).compile());
                owner.msg(plugin.getLocale().getMessage("auction.end.ownersell").set(this).compile());
                plugin.log("[" + id + "] END winner(" + winner.getName() + "), price(" + getEffectiveBid() + ")");
                return;
        }

        @Override
	public void stop() {
                end();
                if (taskid != -1) {
                        plugin.getServer().getScheduler().cancelTask(taskid);
                        taskid = -1;
                }
                state = AuctionState.ENDED;
        }

        @Override
        public void cancel() {
                owner.warn(plugin.getLocale().getMessage("auction.cancel.toowner").set(this).compile());
                ItemDelivery.schedule(owner, item, this);
                plugin.getServer().getScheduler().cancelTask(taskid);
                taskid = -1;
                state = AuctionState.ENDED;
	}

        @Override
        public Merchant getWinner() {
                if (bids.isEmpty()) {
                        return null;
                }
                return bids.getFirst().getBidder();
        }

        private int getBid(Merchant merchant) throws IllegalArgumentException {
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
        private int getMaxBid() {
                if (bids.isEmpty()) {
                        return minbid;
                }
                return bids.getFirst().getAmount();
        }

        @Override
        public int getWinningBid() {
                return getEffectiveBid();
        }

        /**
         * Get the amount that the current winner would have to
         * pay, should he win.
         */
        private int getEffectiveBid() {
                if (bids.isEmpty()) return 0;
                Merchant winner = getWinner();
                if (bids.size() == 1) return minbid;
                for (Bid bid : bids) {
                        if (!bid.getBidder().equals(winner)) {
                                return Math.min(bid.getAmount() + minIncrement, getMaxBid());
                        }
                }
                return minbid;
        }

        /**
         * Get the minimum amount that a new bidder has to make to
         * be accepted.
         */
        @Override
        public int getMinimalBid() {
                if (bids.isEmpty()) return minbid;
                return getEffectiveBid() + minIncrement;
        }

        /**
         * Insert a new bid and assert descending order of bids by
         * amount. Assume validity of bid.
         */
        private void addBid(Bid bid) {
                if (bids.isEmpty()) {
                        bids.add(bid);
                        return;
                }
                for (ListIterator<Bid> iter = bids.listIterator(); iter.hasNext();) {
                        Bid other = iter.next();
                        if (bid.getAmount() > other.getAmount()) {
                                iter.previous();
                                iter.add(bid);
                                return;
                        }
                }
        }
        
        @Override
	public boolean bid(Merchant bidder, int bid) {
		if (bidder.equals(owner) && bidder != BankMerchant.getInstance()) {
			bidder.warn(plugin.getLocale().getMessage("auction.bid.ownauction").set(this, bidder).compile());
			return false;
		}
		if (bidder.equals(getWinner()) && bid <= getMaxBid() && bidder != BankMerchant.getInstance()) {
			bidder.warn(plugin.getLocale().getMessage("auction.bid.underbidself").set(this, bidder).compile());
		 	return false;
		}
                if (bid < getMinimalBid()) {
			bidder.warn(plugin.getLocale().getMessage("auction.bid.bidtoosmall").set(this, bidder).compile());
                        return false;
                }
                if (!bidder.hasAmount(bid)) {
			bidder.warn(plugin.getLocale().getMessage("auction.bid.toopoor").set(this, bidder).compile());
                        return false;
                }
                int oldAmount = getEffectiveBid();
                Merchant oldWinner = getWinner();
                addBid(new Bid(bidder, bid));
                int newAmount = getEffectiveBid();
                Merchant newWinner = getWinner();
                if (oldWinner == null || (newAmount != oldAmount && oldWinner.equals(bidder))) {
                        // overbid, first winner or overbid yourself with price change (corner case)
                        bidder.msg(plugin.getLocale().getMessage("auction.bid.win").set(this, bidder).compile());
                        plugin.broadcast(plugin.getLocale().getMessage("auction.announce.newprice").set(this, bidder).compile());
                } else if (!newWinner.equals(oldWinner)) {
                        // new beats old
                        bidder.msg(plugin.getLocale().getMessage("auction.bid.win").set(this, bidder).compile());
                        plugin.broadcast(plugin.getLocale().getMessage("auction.announce.newwinner").set(this, bidder).compile());
                } else if (newAmount != oldAmount) {
                        // underbid
                        bidder.msg(plugin.getLocale().getMessage("auction.bid.fail").set(this, bidder).compile());
                        plugin.broadcast(plugin.getLocale().getMessage("auction.announce.underbid").set(this, bidder).compile());
                }
                return true;
        }

        @Override
        public Map<String, Object> serialize() {
                Map<String, Object> result = new HashMap<String, Object>();
                result.put("owner", owner);
                result.put("minbid", minbid);
                result.put("timeleft", timeLeft);
                result.put("state", state.ordinal());
                result.put("item", item);
                result.put("bids", bids);
                return result;
        }

        @SuppressWarnings("unchecked")
        public static TimedAuction deserialize(Map<String, Object> map) {
                CraftBayPlugin plugin = CraftBayPlugin.getInstance();
                Merchant owner = (Merchant)map.get("owner");
                Item item = (Item)map.get("item");
                TimedAuction result = new TimedAuction(plugin, owner, item);
                result.timeLeft = (Integer)map.get("timeleft");
                result.minbid = (Integer)map.get("minbid");
                result.state = AuctionState.values()[(Integer)map.get("state")];
                result.bids = new LinkedList<Bid>((List<Bid>)map.get("bids"));
                return result;
        }
}