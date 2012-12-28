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

public class MoneyAmount {
        int amount;

        public MoneyAmount(int amount) {
                this.amount = amount;
        }

        @Override
        public String toString() {
                /* 
                 * Essentials Economy is known to throw an
                 * exception here if it is called before being
                 * enabled. Perhaps Vault should add it to its
                 * soft-depends but CraftBay will do no such
                 * thing; so the exception must be caught here.
                 *
                 * This can be considered bad habit since the
                 * Exception is simply muted which should never be
                 * done. But on the other hand, how much could
                 * possibly go wrong when formatting a double
                 * number.
                 */
                try {
                        return CraftBayPlugin.getInstance().getEco().format((double)amount);
                } catch (RuntimeException e) {
                        return "" + amount;
                }
        }
}
