package edu.self.startux.craftBay.economy;

import com.cavetale.money.Money;
import edu.self.startux.craftBay.CraftBayPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.OfflinePlayer;

@RequiredArgsConstructor
public final class MoneyEconomy implements Economy {
    private final CraftBayPlugin plugin;

    @Override
    public MoneyEconomy setup() {
        Money.format(0.0);
        plugin.getLogger().info("Using Money Economy");
        return this;
    }

    @Override
    public boolean has(OfflinePlayer off, double money) {
        return Money.has(off.getUniqueId(), money);
    }

    @Override
    public double get(OfflinePlayer off) {
        return Money.get(off.getUniqueId());
    }

    @Override
    public boolean give(OfflinePlayer off, double money, String message) {
        return Money.give(off.getUniqueId(), money, plugin, message);
    }

    @Override
    public boolean take(OfflinePlayer off, double money, String message) {
        return Money.take(off.getUniqueId(), money, plugin, message);
    }

    @Override
    public String format(double amount) {
        return Money.format(amount);
    }
}
