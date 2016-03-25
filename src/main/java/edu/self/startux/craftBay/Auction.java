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

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public interface Auction extends ConfigurationSerializable {
        /**
         * Get this auction's id
         * @return the id
         */
        public int getId();

        /**
         * The AuctionScheduler should set the id with this method.
         * @param id the id
         */
        public void setId(int id);

        /**
         * Get the state of this auction
         * @return the state
         */
        public AuctionState getState();
        public void setState(AuctionState state);

        /**
         * Get the item that is being auctioned
         * @return the item
         */
        public Item getItem();

        /**
         * Get the owner of this auction
         * @return the owner
         */
        public Merchant getOwner();

        /**
         * Get the winner of this auction
         * @return the winner or null if there isn't one
         */
        public Merchant getWinner();

        /**
         * Start this auction.
         */
        public void start();

        /**
         * Attempt to place a bid.
         * @param bidder the Merchant attempting to bid
         * @param bid the bid amount
         */
	public boolean bid(Merchant bidder, MoneyAmount bid);

        /**
         * Stop this auction.
         */
        public void stop();

        /**
         * End this auction legitmately.
         */
        public void end();

        /**
         * Cancel this auction.
         */
        public void cancel();

        /**
         * Get the minimum amount that a new bidder has to make to
         * be accepted.
         */
        public MoneyAmount getMinimalBid();
        /**
         * Get the highest bid that was placed
         */
        public MoneyAmount getMaxBid();
        /**
         * Get the winning bid
         */
        public MoneyAmount getWinningBid();

        public int getTimeLeft();
        public void setTimeLeft(int time);

        public MoneyAmount getStartingBid();
        public void setStartingBid(MoneyAmount amount);

        public MoneyAmount getFee();
        public void setFee(MoneyAmount fee);

        public void log(String msg);
        public List<String> getLog();
}
