/*
 * This file is part of Lore.
 *
 * Copyright (c) 2013, AlmuraDev <http://www.almuradev.com/>
 * Lore is licensed under the Almura Development License.
 *
 * Lore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Lore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License. If not,
 * see <http://www.gnu.org/licenses/> for the GNU General Public License.
 */
package com.almuradev.lore.listener;

import com.almuradev.lore.LorePlugin;
import com.almuradev.lore.util.VaultUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class LoreListener implements Listener {
	private final LorePlugin plugin;

	public LoreListener(LorePlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (!player.hasPlayedBefore() && VaultUtil.hasPermission(player.getName(), player.getWorld().getName(), "lore.join.obtain")) {
			player.getInventory().addItem(plugin.getConfiguration().getBook());
			if (VaultUtil.hasPermission(player.getName(), player.getWorld().getName(), "lore.join.message") && plugin.getConfiguration().getJoinMessage() != null && !plugin.getConfiguration().getJoinMessage().isEmpty()) {
				player.sendMessage(plugin.getConfiguration().getJoinMessage());
			}
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();

		if (VaultUtil.hasPermission(player.getName(), player.getWorld().getName(), "lore.respawn.obtain")) {
			player.getInventory().addItem(plugin.getConfiguration().getBook());
			if (VaultUtil.hasPermission(player.getName(), player.getWorld().getName(), "lore.respawn.message") && plugin.getConfiguration().getJoinMessage() != null && !plugin.getConfiguration().getJoinMessage().isEmpty()) {
				player.sendMessage(plugin.getConfiguration().getRespawnMessage());
			}
		}
	}
}
