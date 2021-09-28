package edu.self.startux.craftBay.economy;

import edu.self.startux.craftBay.CraftBayPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public final class NullEconomy implements Economy {
    @Override
    public Economy setup() {
        return this;
    }

    @Override
    public boolean has(OfflinePlayer off, double money) {
        return false;
    }

    @Override
    public double get(OfflinePlayer off) {
        return 0;
    }

    @Override
    public boolean give(OfflinePlayer off, double money, String message) {
        return false;
    }

    @Override
    public boolean take(OfflinePlayer off, double money, String message) {
        return false;
    }

    @Override
    public String format(double amount) {
        return "" + amount;
    }
}
