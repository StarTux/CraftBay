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

public class MoneyAmount implements Comparable<MoneyAmount> {
        double amount;

        public MoneyAmount(double amount) {
                this.amount = amount;
        }

        /**
         * This constructor is there mostly for support of legacy
         * auction.yml files where money wasexpressed solely as
         * Integer. The additional support for String and Object
         * is just a catch-call precaution. Use it sparingly.
         */
        public MoneyAmount(Object o) {
                if (o instanceof Number) amount = ((Number)o).doubleValue();
                else if (o instanceof String) {
                        try {
                                amount = Double.parseDouble((String)o);
                        } catch (NumberFormatException nfe) {
                                amount = 0.0;
                        }
                } else {
                        try {
                                amount = Double.parseDouble(o.toString());
                        } catch (NumberFormatException nfe) {
                                amount = 0.0;
                        }
                }
        }

        public double getDouble() {
                return amount;
        }

        @Override
        public boolean equals(Object other) {
                if (!(other instanceof MoneyAmount)) return false;
                return amount == ((MoneyAmount)other).amount;
        }

        @Override
        public int compareTo(MoneyAmount other) {
                return new Double(amount).compareTo(other.amount);
        }

        @Override
                public String toString() {
                try {
                        return CraftBayPlugin.getInstance().getEco().format((double)amount);
                } catch (RuntimeException e) {
                        return "" + amount;
                }
        }
}
