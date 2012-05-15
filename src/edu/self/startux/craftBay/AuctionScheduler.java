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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class AuctionScheduler implements Runnable {
        private CraftBayPlugin plugin;
        private int maxQueue = 10;
        private int maxHistory = 10;
        private LinkedList<Auction> queue = new LinkedList<Auction>();
        private LinkedList<Auction> history = new LinkedList<Auction>();
        private LinkedList<ItemDelivery> deliveries = new LinkedList<ItemDelivery>();
        private Auction current;
        private int taskid = -1;
        private FileConfiguration conf;

        private File getSaveFile() {
                File folder = plugin.getDataFolder();
                if (!folder.exists()) folder.mkdir();
                return new File(folder, "auctions.yml");
        }


        public AuctionScheduler(CraftBayPlugin plugin) {
                this.plugin = plugin;
        }

        public void enable() {
                maxQueue = plugin.getConfig().getInt("maxqueue");
                maxHistory = plugin.getConfig().getInt("maxhistory");
                load();
        }

        public void disable() {
                save();
        }

        public void queueAuction(Auction auction) {
                queue.addFirst(auction);
                soon();
        }

        public void queueDelivery(ItemDelivery delivery) {
                deliveries.add(delivery);
        }

        public void soon() {
                if (taskid != -1) return;
                taskid = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, 0l);
                if (taskid == -1) {
                        plugin.log("AuctionScheduler failed scheduleSyncDelayedTask()", Level.SEVERE);
                }
        }

        @Override
        public void run() {
                taskid = -1;
                schedule();
                // System.out.println("queue: " + + queue.size());
                // System.out.println("history: " + history.size());
                // System.out.println("current: " + (current == null ? "null " : "some"));
        }

        public void schedule() {
                for (Iterator<ItemDelivery> it = deliveries.iterator(); it.hasNext();) {
                        ItemDelivery delivery = it.next();
                        if (delivery.deliver()) it.remove();
                }
                if (current != null) {
                        if (current.getState() != AuctionState.ENDED) return;
                        history.addFirst(current);
                        while (history.size() > maxHistory) history.removeLast();
                        current = null;
                }
                if (!queue.isEmpty()) {
                        current = queue.removeLast();
                        if (current != null) current.start();
                }
        }

        public Auction getCurrentAuction() {
                return current;
        }

        private void save() {
                conf.set("current", current);
                conf.set("queue", new ArrayList<Object>(queue));
                conf.set("history", new ArrayList<Object>(history));
                try {
                        conf.save(getSaveFile());
                } catch (IOException ioe) {
                        System.err.println(ioe);
                        ioe.printStackTrace();
                }
        }

        @SuppressWarnings("unchecked")
        private void load() {
                conf = YamlConfiguration.loadConfiguration(getSaveFile());
                if (conf.getList("history") != null) history = new LinkedList<Auction>((List<Auction>)conf.getList("history"));
               if (conf.getList("queue") != null) queue = new LinkedList<Auction>((List<Auction>)conf.getList("queue"));
                 if (conf.get("current") != null) current = (Auction)conf.get("current");
                if (current != null) current.start();
        }

        public List<Auction> getQueue() {
                return new ArrayList(queue);
        }

        public List<Auction> getHistory() {
                return new ArrayList(history);
        }

        public Auction getById(int id) {
                if (current.getId() == id) return current;
                for (Auction auction : history) if (auction.getId() == id) return auction;
                for (Auction auction : queue) if (auction.getId() == id) return auction;
                return null;
        }
}