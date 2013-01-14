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

import edu.self.startux.craftBay.locale.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a fake item that is nothing but a name.
 */
public class FakeItem implements Item {
        private String name;

        public FakeItem(String name) {
                this.name = name;
        }

        @Override
        public Item clone() {
                return new FakeItem(name);
        }

        @Override
        public String getName() {
                return name;
        }

        @Override
        public String getDescription() {
                return getName();
        }

        @Override
        public ItemAmount getAmount() {
                return new ItemAmount(1);
        }

        @Override
        public String getItemInfo() {
                return "";
        }

        @Override
        public int getId() {
                return 0;
        }

        @Override
        public int getDamage() {
                return 0;
        }

        @Override
        public boolean has(Merchant merchant) {
                if (merchant instanceof BankMerchant) {
                        return true;
                }
                return false;
        }

        @Override
        public Item take(Merchant merchant) {
                if (merchant instanceof BankMerchant) {
                        return this;
                }
                return null;
        }

        @Override
        public boolean give(Merchant merchant) {
                return true;
        }

        @Override
        public Map<String, Object> serialize() {
                Map<String, Object> result = new HashMap<String, Object>();
                result.put("name", name);
                return result;
        }

        public static FakeItem deserialize(Map<String, Object> map) {
                return new FakeItem((String)map.get("name"));
        }

        @Override
        public String toString() {
                return name;
        }
}
