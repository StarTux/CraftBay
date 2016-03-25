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

import edu.self.startux.craftBay.locale.Message;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

public class BankMerchant implements Merchant {
        private static BankMerchant instance = null;

        public static BankMerchant getInstance() {
                if (instance == null) instance = new BankMerchant();
                return instance;
        }

        private BankMerchant() {}

        @Override
        public String getName() {
                return "The Bank";
        }

        @Override
        public boolean hasAmount(MoneyAmount amount) {
                return true;
        }

        @Override
        public boolean giveAmount(MoneyAmount amount) {
                return true;
        }

        @Override
        public boolean takeAmount(MoneyAmount amount) {
                return true;
        }

        @Override
        public boolean hasItem(ItemStack lot) {
                return true;
        }

        @Override
        public boolean takeItem(ItemStack lot) {
                return true;
        }

        /**
         * Check if the Bank has a Bukkit permission.
         * @param permission the permission
         * @return true
         */
        public boolean hasPermission(String permission) {
                return true;
        }

        @Override
        public void msg(Message msg) {}

        @Override
        public void warn(Message msg) {}

        @Override
        public boolean equals(Object o) {
                if (o == null) {
                        return false;
                }
                if (o instanceof BankMerchant) {
                        return true;
                }
                return false;
        }

        @Override
        public Map<String, Object> serialize() {
                return new HashMap<String, Object>();
        }

        public static BankMerchant deserialize(Map<String, Object> map) {
                return getInstance();
        }

        @Override
        public boolean isListening() {
                return true;
        }

        @Override
        public Merchant clone() {
                return new BankMerchant();
        }
}
