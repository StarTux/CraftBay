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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class AuctionScheduler implements Runnable {
        private CraftBayPlugin plugin;
        private LinkedList<Auction> queue = new LinkedList<Auction>();
        private LinkedList<Auction> history = new LinkedList<Auction>();
        private Auction current;
        private int taskid = -1;
        private FileConfiguration conf;
        private int nextAuctionId = 0;
        private LinkedList<ItemDelivery> deliveries = new LinkedList<ItemDelivery>();
        private Map<Integer, Auction> idMap = new HashMap<Integer, Auction>();

        private File getSaveFile() {
                File folder = plugin.getDataFolder();
                if (!folder.exists()) folder.mkdir();
                return new File(folder, "auctions.yml");
        }


        public AuctionScheduler(CraftBayPlugin plugin) {
                this.plugin = plugin;
        }

        public void enable() {
                load();
        }

        public void disable() {
                if (current != null) current.stop();
                save();
        }

        private void addAuction(Auction auction) {
                idMap.put(auction.getId(), auction);
        }

        private void removeAuction(Auction auction) {
                idMap.remove(auction.getId());
        }

        public Auction getById(int id) {
                return idMap.get(id);
        }

        public void queueAuction(Auction auction) {
                auction.setId(nextAuctionId++);
                addAuction(auction);
                queue.addFirst(auction);
                soon();
        }

        public boolean unqueueAuction(Auction auction) {
                return queue.remove(auction);
        }

        public void soon(long delay) {
                if (taskid != -1) return;
                taskid = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, delay);
                if (taskid == -1) {
                        plugin.getLogger().severe("AuctionScheduler failed scheduleSyncDelayedTask()");
                }
        }

        public void soon() {
                soon(0l);
        }

        @Override
        public void run() {
                taskid = -1;
                schedule();
        }

        private void historyAuction(Auction auction) {
                history.addFirst(auction);
                int maxHistory = plugin.getConfig().getInt("maxhistory");
                while (history.size() > maxHistory) {
                        removeAuction(history.removeLast());
                }
        }

        public void schedule() {
                ItemDelivery.deliverAll();
                boolean dirty = false;
                if (current != null) {
                        if (current.getState() == AuctionState.ENDED) {
                                plugin.getAuctionHouse().endAuction(current);
                                historyAuction(current);
                                current = null;
                                dirty = true;
                        } else if (current.getState() == AuctionState.CANCELED) {
                                historyAuction(current);
                                current = null;
                                dirty = true;
                        } else if (current.getState() == AuctionState.QUEUED) {
                                current.start();
                                dirty = true;
                        }
                        // current may have changed
                        if (current == null) {
                                soon(plugin.getConfig().getLong("auctionpause") * 20l);
                        }
                } else {
                        while (current == null && !queue.isEmpty()) {
                                Auction next = queue.removeLast();
                                if (next.getState() == AuctionState.CANCELED) {
                                        historyAuction(next);
                                } else {
                                        current = next;
                                        current.start();
                                }
                                dirty = true;
                        }
                }
                if (dirty) {
                        save();
                }
        }

        public Auction getCurrentAuction() {
                return current;
        }


        public List<Auction> getQueue() {
                return new ArrayList<Auction>(queue);
        }

        public List<Auction> getHistory() {
                return new ArrayList<Auction>(history);
        }

        public List<Auction> getAllAuctions() {
                List<Auction> result = new ArrayList<Auction>(queue.size() + history.size() + 1);
                result.addAll(history);
                result.add(current);
                result.addAll(queue);
                return result;
        }

        public boolean canQueue() {
                int maxQueue = plugin.getConfig().getInt("maxqueue");
                return queue.size() < maxQueue;
        }

        public void queueDelivery(ItemDelivery delivery) {
                deliveries.add(delivery);
        }

        public void checkDeliveries() {
                long currentDate = System.currentTimeMillis();
                for (Iterator<ItemDelivery> it = deliveries.iterator(); it.hasNext();) {
                        ItemDelivery delivery = it.next();
                        if (delivery.deliver()) {
                                it.remove();
                        } else {
                                if (TimeUnit.MILLISECONDS.toDays(Math.max(0, currentDate - delivery.getCreationDate().getTime())) > plugin.getConfig().getLong("itemexpiry")) {
                                        plugin.getLogger().info(String.format("DROP recipient='%s' item='%s' created='%s'", delivery.getRecipient().getName(), delivery.getItem().toString(), delivery.getCreationDate().toString()));
                                        it.remove();
                                }
                        }
                }
        }

        private void save() {
                if (plugin.getDebugMode()) plugin.getLogger().info("saving");
                conf.set("current", current);
                conf.set("queue", new ArrayList<Object>(queue));
                conf.set("history", new ArrayList<Object>(history));
                conf.set("deliveries", new ArrayList<Object>(deliveries));
                try {
                        conf.save(getSaveFile());
                } catch (IOException ioe) {
                        System.err.println(ioe);
                        ioe.printStackTrace();
                }
        }

        @SuppressWarnings("unchecked")
        private void load() {
                try {
                        conf = YamlConfiguration.loadConfiguration(getSaveFile());
                } catch (Exception e) {
                        e.printStackTrace();
                        getSaveFile().delete();
                        conf = YamlConfiguration.loadConfiguration(getSaveFile());
                }
                if (conf.getList("history") != null) history = new LinkedList<Auction>((List<Auction>)conf.getList("history"));
                else history = new LinkedList<Auction>();
                if (conf.getList("queue") != null) queue = new LinkedList<Auction>((List<Auction>)conf.getList("queue"));
                else queue = new LinkedList<Auction>();
                if (conf.get("current") != null) {
                        current = (Auction)conf.get("current");
                        if (current != null && current.getState() == AuctionState.RUNNING) {
                                current.start();
                        }
                } else {
                        current = null;
                }
                int i = history.size() - 1;
                for (Auction auction : history) auction.setId(i--);
                if (current != null) current.setId(history.size());
                i = history.size() + queue.size() - (current == null ? 1 : 0);
                for (Auction auction : queue) auction.setId(i--);
                nextAuctionId = history.size() + queue.size() + (current != null ? 1 : 0);
                for (Auction auction : queue) addAuction(auction);
                if (current != null) addAuction(current);
                for (Auction auction : history) addAuction(auction);
                if (conf.get("deliveries") != null) deliveries = new LinkedList<ItemDelivery>((List<ItemDelivery>)conf.getList("deliveries"));
        }
}
