package edu.self.startux.craftBay.economy;

import edu.self.startux.craftBay.CraftBayPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public interface Economy {
    Economy setup();
    boolean has(OfflinePlayer off, double money);
    double get(OfflinePlayer off);
    boolean give(OfflinePlayer off, double money, String message);
    boolean take(OfflinePlayer off, double money, String message);
    String format(double amount);

    static Economy get(CraftBayPlugin plugin) {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("Money")) {
                if (Bukkit.getPluginManager().getPlugin("Money").getClass().getName().equals("com.cavetale.money.MoneyPlugin")) {
                    Economy result = new MoneyEconomy(plugin).setup();
                    if (result != null) return result;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
                return new VaultEconomy(plugin).setup();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        plugin.getLogger().warning("No economy provider found! Falling back to NullEconomy");
        return new NullEconomy();
    }
}
