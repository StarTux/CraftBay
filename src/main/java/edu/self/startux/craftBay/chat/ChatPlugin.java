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

package edu.self.startux.craftBay.chat;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public interface ChatPlugin {
    /**
     * Initialization routine. Configure according to section.
     * @param section the ConfigurationSection
     * @return true on success, false if there were errors
     */
    boolean enable(ConfigurationSection section);

    /**
     * Destruction routine. This is where data should be
     * saved, if any.
     */
    void disable();

    /**
     * Broadcast a Component to everyone listening.
     * @param component the component
     */
    void broadcast(Component component);

    /**
     * Set whether a player is listening or not.
     * @param player the player
     * @param on true if the player should listen, false otherwise
     * @return true if player is now listening, false otherwise
     */
    boolean listen(Player player, boolean on);

    /**
     * Check whether a player is listening or not.
     * @param player the player
     * @return true if the player is listening, false otherwise
     */
    boolean isListening(Player player);
}
