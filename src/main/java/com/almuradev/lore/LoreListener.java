/**
 * This file is part of Lore.
 *
 * Copyright (c) 2013 AlmuraDev <http://almuradev.com/>
 * Lore is licensed under the GNU General Public License Version 3.
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
package com.almuradev.lore;

import java.io.FileNotFoundException;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.meta.BookMeta;

public class LoreListener implements Listener {
    private final LorePlugin plugin;

    public LoreListener(LorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPlayedBefore()) {
            return;
        }
        if (!player.hasPermission("lore.join.obtain")) {
            return;
        }
        for (String book : plugin.getConfiguration().getAvailableBooks()) {
            try {
                if (player.getInventory().contains(plugin.getConfiguration().getItem(book))) {
                    return;
                }
                if (plugin.getConfiguration().getConfig(book).getBoolean("join")) {
                    player.getInventory().addItem(plugin.getConfiguration().getItem(book));
                    player.sendMessage(plugin.getConfig().getString("messages.join"));
                }
            } catch (FileNotFoundException e) {
                plugin.getLogger().log(Level.WARNING, "An error occurred while attempting to fetch " + book + " during PlayerJoinEvent.", e);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("lore.respawn.obtain")) {
            return;
        }
        for (String book : plugin.getConfiguration().getAvailableBooks()) {
            try {
                if (player.getInventory().contains(plugin.getConfiguration().getItem(book))) {
                    return;
                }
                if (plugin.getConfiguration().getConfig(book).getBoolean("respawn")) {
                    player.getInventory().addItem(plugin.getConfiguration().getItem(book));
                    player.sendMessage(plugin.getConfig().getString("messages.respawn"));
                }
            } catch (FileNotFoundException e) {
                plugin.getLogger().log(Level.WARNING, "An error occurred while attempting to fetch " + book + " during PlayerRespawnEvent.", e);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (event.getItemDrop() == null) {
            return;
        }
        if (!(event.getItemDrop().getItemStack().getItemMeta() instanceof BookMeta)) {
            return;
        }
        if (player.hasPermission("lore.sticky.bypass")) {
            return;
        }
        for (String book : plugin.getConfiguration().getAvailableBooks()) {
            try {
                if (!plugin.getConfiguration().getConfig(book).getBoolean("sticky")) {
                    return;
                }
                if (event.getItemDrop().getItemStack().getItemMeta().equals(plugin.getConfiguration().getItem(book).getItemMeta())) {
                    player.sendMessage(plugin.getConfig().getString("messages.sticky"));
                    event.setCancelled(true);
                }
            } catch (FileNotFoundException e) {
                plugin.getLogger().log(Level.WARNING, "An error occurred while attempting to fetch " + book + " during PlayerRespawnEvent.", e);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = null;
        if (event.getWhoClicked() instanceof Player) {
            player = (Player) event.getWhoClicked();
        }
        if (player == null) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        if (!(event.getCurrentItem().getItemMeta() instanceof BookMeta)) {
            return;
        }
        if (event.getInventory().getType() == InventoryType.PLAYER) {
            return;
        }
        if (player.hasPermission("lore.sticky.bypass")) {
            return;
        }
        for (String book : plugin.getConfiguration().getAvailableBooks()) {
            try {
                if (!plugin.getConfiguration().getConfig(book).getBoolean("sticky") && event.getInventory().getType() != InventoryType.MERCHANT) {
                    return;
                }
                if (event.getCurrentItem().getItemMeta().equals(plugin.getConfiguration().getItem(book).getItemMeta())) {
                    if (event.getInventory().getType() != InventoryType.MERCHANT) {
                        player.sendMessage(plugin.getConfig().getString("messages.sticky"));
                        event.setCancelled(true);
                    } else if (event.getInventory().getType() == InventoryType.MERCHANT && !plugin.getConfig().getBoolean("allow-villager-trades")) {
                        player.sendMessage(plugin.getConfig().getString("messages.villager"));
                        event.setCancelled(true);
                    }
                }
            } catch (FileNotFoundException e) {
                plugin.getLogger().log(Level.WARNING, "An error occurred while attempting to fetch " + book + " during PlayerRespawnEvent.", e);
            }
        }
    }
}
