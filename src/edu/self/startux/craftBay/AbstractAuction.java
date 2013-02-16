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

import java.util.logging.Level;
import java.util.List;
import java.util.LinkedList;

public abstract class AbstractAuction implements Auction, Runnable {
	private CraftBayPlugin plugin;
        private AuctionState state;
        private Item item;
	private Merchant owner;
        private int auctionId;
	private int taskid = -1;
        private MoneyAmount fee;
        protected List<String> log = new LinkedList<String>();

        public AbstractAuction(CraftBayPlugin plugin, Merchant owner, Item item) {
                this.plugin = plugin;
                this.owner = owner;
                this.item = item;
        }

        public CraftBayPlugin getPlugin() {
                return plugin;
        }

        @Override
        public void setState(AuctionState state) {
                this.state = state;
        }

        @Override
        public AuctionState getState() {
                return state;
        }

        @Override
        public int getId() {
                return auctionId;
        }

        @Override
        public void setId(int id) {
                auctionId = id;
        }

        @Override
        public Item getItem() {
                return item;
        }

        @Override
        public Merchant getOwner() {
                return owner;
        }

        @Override
        public MoneyAmount getFee() {
                return fee;
        }

        @Override
        public void setFee(MoneyAmount fee) {
                this.fee = fee;
        }

        @Override
        public void log(String msg) {
                log.add(msg);
                plugin.getLogger().info("[" + auctionId + "] " + msg);
        }

        @Override
        public List<String> getLog() {
                return log;
        }

        protected void scheduleTick(boolean on) {
                if (on) {
                        if (taskid == -1) {
                                taskid = getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(getPlugin(), this, 0l, 20l);
                                if (taskid == -1) {
                                        getPlugin().getLogger().severe("TimedAuction failed scheduleSyncRepeatingTask()");
                                }
                        }
                } else {
                        if (taskid != -1) {
                                getPlugin().getServer().getScheduler().cancelTask(taskid);
                                taskid = -1;
                        }
                }
        }

        @Override
        public void run() {
                tick();
        }

        abstract public void tick();
}
