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
                ITEM(1),
                STACK(64),
                CHEST(64 * 9 * 3),
                DOUBLE_CHEST(64 * 9 * 6),
                INVENTORY(64 * 9 * 4),
                HAND,
                ALL;

                private int multiplier;

                private Unit() {
                        multiplier = -1;
                }

                private Unit(int multiplier) {
                        this.multiplier = multiplier;
                }

                public int getMultiplier() {
                        return multiplier;
                }
        }

        private Unit unit;
        private int amount;

        public ItemAmount(int amount) {
                unit = Unit.ITEM;
                this.amount = amount;
        }
}