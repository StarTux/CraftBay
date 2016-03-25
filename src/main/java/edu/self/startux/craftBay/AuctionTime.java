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

public class AuctionTime {
        private int time;

        public AuctionTime(int time) {
                this.time = time;
        }

        @Override
        public String toString() {
                int minutes = time / 60;
                int seconds = time % 60;
                if (minutes == 0) {
                        if (seconds == 1) return String.format("%d " + CraftBayPlugin.getInstance().getMessage("item.second.Singular"), seconds);
                        return String.format("%d " + CraftBayPlugin.getInstance().getMessage("item.second.Plural"), seconds);
                }
                if (seconds == 0) {
                        if (minutes == 1) return String.format("%d " + CraftBayPlugin.getInstance().getMessage("item.minute.Singular"), minutes);
                        return String.format("%d " + CraftBayPlugin.getInstance().getMessage("item.minute.Plural"), minutes);
                }
                return String.format("%02d:%02d", minutes, seconds);
        }

        public int getSeconds() {
                return time;
        }
}
