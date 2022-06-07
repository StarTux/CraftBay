package edu.self.startux.craftBay.economy;

import com.cavetale.core.money.Money;
import edu.self.startux.craftBay.CraftBayPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.OfflinePlayer;

@RequiredArgsConstructor
public final class CoreEconomy implements Economy {
    private final CraftBayPlugin plugin;

    @Override
    public CoreEconomy setup() {
        Money.get().format(0.0);
        plugin.getLogger().info("Using Core Economy");
        return this;
    }

    @Override
    public boolean has(OfflinePlayer off, double money) {
        return Money.get().has(off.getUniqueId(), money);
    }

    @Override
    public double get(OfflinePlayer off) {
        return Money.get().get(off.getUniqueId());
    }

    @Override
    public boolean give(OfflinePlayer off, double money, String message) {
        return Money.get().give(off.getUniqueId(), money, plugin, message);
    }

    @Override
    public boolean take(OfflinePlayer off, double money, String message) {
        return Money.get().take(off.getUniqueId(), money, plugin, message);
    }

    @Override
    public String format(double amount) {
        return Money.get().format(amount);
    }
}
