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

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import java.util.Map;
import java.util.HashMap;

/**
 * Bid is a simple carrier of information about a placed bid.
 * This class can be compared by its natural ordering to determine
 * if one bid beats another one.
 */
public class Bid implements Comparable<Bid>, ConfigurationSerializable {
        private Merchant bidder;
        private double amount;

        public Bid(Merchant bidder, double amount) {
                this.bidder = bidder;
                this.amount = amount;
        }

        public Bid(Merchant bidder, MoneyAmount amount) {
                this(bidder, amount.getDouble());
        }

        public Merchant getBidder() {
                return bidder;
        }

        public double getAmount() {
                return amount;
        }

        public void setAmount(double amount) {
                this.amount = amount;
        }

        public void setAmount(MoneyAmount amount) {
                this.amount = amount.getDouble();
        }

        @Override
        public int compareTo(Bid o) {
                if (amount < o.amount) return -1;
                if (amount > o.amount) return 1;
                return 0;
        }

        @Override
        public Map<String, Object> serialize() {
                Map<String, Object> result = new HashMap<String, Object>();
                result.put("bidder", bidder.clone());
                result.put("amount", amount);
                return result;
        }

        @SuppressWarnings("unchecked")
        public static Bid deserialize(Map<String, Object> map) {
                return new Bid((Merchant)map.get("bidder"), new MoneyAmount(map.get("amount")).getDouble());
        }
}
