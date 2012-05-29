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

public class ItemAmount {
        public static enum Unit {
                INVENTORY(9 * 4),
                DOUBLE_CHEST(9 * 6),
                CHEST(9 * 3),
                STACK(1);

                private int multiplier;

                private Unit() {
                        this(0);
                }

                private Unit(int multiplier) {
                        this.multiplier = multiplier;
                }

                public int getMultiplier(int stackSize) {
                        return multiplier * stackSize;
                }

                private String getNodeName() {
                        if (this == DOUBLE_CHEST) return "doubleChest";
                        return name().toLowerCase();
                }

                public String getMessageName(int amount) {
                        if (amount == 1) return CraftBayPlugin.getInstance().getMessage("item." + getNodeName() + ".Singular").toString();
                        return CraftBayPlugin.getInstance().getMessage("item." + getNodeName() + ".Plural").toString();
                }
        }

        private int amount;
        private int stackSize;

        public ItemAmount(int amount, int stackSize) {
                this.amount = amount;
                this.stackSize = stackSize;
        }

        public ItemAmount(int amount) {
                this(amount, 1);
        }

        public int getInt() {
                return amount;
        }

        @Override public String toString() {
                if (stackSize < 16) return "" + amount;
                for (Unit unit : Unit.values()) {
                        if (amount % unit.getMultiplier(stackSize) == 0) {
                                int count = amount / unit.getMultiplier(stackSize);
                                return "" + count + " " + unit.getMessageName(count);
                        }
                }
                return "" + amount;
        }
}