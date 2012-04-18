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

/**
 * Bid is a simple carrier of information about a placed bid, some
 * of which really meaningful to the Auction class. This class can
 * be compared by its natural ordering to determine if one bid
 * beats another one.
 */
public class Bid implements Comparable<Bid> {
        private Merchant bidder;
        private int amount;
        private int count = 0;

        public Bid(Merchant bidder, int amount) {
                this.bidder = bidder;
                this.amount = amount;
        }

        public Merchant getBidder() {
                return bidder;
        }

        public int getAmount() {
                return amount;
        }

        public void setAmount(int amount) {
                this.amount = amount;
        }

        /**
         * The count determines when this bid was placed within an
         * auction, lower numbers meaning earlier. If tow
         * different bids have the same money amount, the earlier
         * one will be worth more as far as compareTo() is
         * concerned. This function sets this count; the initial
         * value is 0.
         * @param count the count
         */
        public void setCount(int count) {
                this.count = count;
        }

        @Override
        public int compareTo(Bid o) {
                if (amount < o.amount) return -1;
                if (amount > o.amount) return 1;
                if (count < o.count) return 1;
                if (count > o.count) return -1;
                return 0; // should never happen;
        }
}