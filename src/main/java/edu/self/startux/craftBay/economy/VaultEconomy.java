package edu.self.startux.craftBay.economy;

import edu.self.startux.craftBay.CraftBayPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

@RequiredArgsConstructor
public final class VaultEconomy implements Economy {
    private final CraftBayPlugin plugin;
    private net.milkbowl.vault.economy.Economy economy;

    @Override
    public VaultEconomy setup() {
        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider =
            Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            this.economy = economyProvider.getProvider();
        }
        if (this.economy == null) return null;
        plugin.getLogger().info("Using Vault Economy");
        return this;
    }

    @Override
    public boolean has(OfflinePlayer off, double money) {
        return economy.has(off, money);
    }

    @Override
    public double get(OfflinePlayer off) {
        return economy.getBalance(off);
    }

    @Override
    public boolean give(OfflinePlayer off, double money, String message) {
        return economy.depositPlayer(off, money).transactionSuccess();
    }

    @Override
    public boolean take(OfflinePlayer off, double money, String message) {
        return economy.withdrawPlayer(off, money).transactionSuccess();
    }

    @Override
    public String format(double amount) {
        return economy.format(amount);
    }
}
