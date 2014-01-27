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
    private final String JOIN_KEY = "join";
    private final String JOIN_MESSAGE_KEY;
    private final String JOIN_PERMISSION_KEY = "lore.join.obtain";
    private final String RESPAWN_KEY = "respawn";
    private final String RESPAWN_MESSAGE_KEY;
    private final String RESPAWN_PERMISSION_KEY = "lore.respawn.obtain";
    private final String STICKY_KEY = "sticky";
    private final String STICKY_MESSAGE_KEY;
    private final String STICKY_PERMISSION_KEY = "lore.sticky.bypass";
    private final String VILLAGER_KEY = "allow-villager-trades";
    private final String VILLAGER_MESSAGE_KEY;

    public LoreListener(LorePlugin plugin) {
        this.plugin = plugin;
        JOIN_MESSAGE_KEY = plugin.getConfig().getString("messages.join");
        RESPAWN_MESSAGE_KEY = plugin.getConfig().getString("messages.respawn");
        STICKY_MESSAGE_KEY = plugin.getConfig().getString("messages.sticky");
        VILLAGER_MESSAGE_KEY = plugin.getConfig().getString("messages.villager");
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPlayedBefore()) {
            return;
        }
        if (!player.hasPermission(JOIN_PERMISSION_KEY)) {
            return;
        }
        for (String book : plugin.getConfiguration().getAvailableBooks()) {
            try {
                if (player.getInventory().contains(plugin.getConfiguration().getBookItem(book))) {
                    return;
                }
                if (plugin.getConfiguration().getBookConfig(book).getBoolean(JOIN_KEY)) {
                    player.getInventory().addItem(plugin.getConfiguration().getBookItem(book));
                    if (JOIN_MESSAGE_KEY != null && !JOIN_MESSAGE_KEY.isEmpty()) {
                        player.sendMessage(JOIN_MESSAGE_KEY);
                    }
                }
            } catch (FileNotFoundException e) {
                plugin.getLogger().log(Level.WARNING, "An error occurred while attempting to fetch " + book + " during PlayerJoinEvent", e);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(RESPAWN_PERMISSION_KEY)) {
            return;
        }
        for (String book : plugin.getConfiguration().getAvailableBooks()) {
            try {
                if (player.getInventory().contains(plugin.getConfiguration().getBookItem(book))) {
                    return;
                }
                if (plugin.getConfiguration().getBookConfig(book).getBoolean(RESPAWN_KEY)) {
                    player.getInventory().addItem(plugin.getConfiguration().getBookItem(book));
                    if (RESPAWN_MESSAGE_KEY != null && !RESPAWN_MESSAGE_KEY.isEmpty()) {
                        player.sendMessage(RESPAWN_MESSAGE_KEY);
                    }
                }
            } catch (FileNotFoundException e) {
                plugin.getLogger().log(Level.WARNING, "An error occurred while attempting to fetch " + book + " during PlayerRespawnEvent", e);
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
        if (player.hasPermission(STICKY_PERMISSION_KEY)) {
            return;
        }
        for (String book : plugin.getConfiguration().getAvailableBooks()) {
            try {
                if (!plugin.getConfiguration().getBookConfig(book).getBoolean(STICKY_KEY)) {
                    return;
                }
                if (event.getItemDrop().getItemStack().getItemMeta().equals(plugin.getConfiguration().getBookItem(book).getItemMeta())) {
                    if (STICKY_KEY != null && !STICKY_MESSAGE_KEY.isEmpty()) {
                        player.sendMessage(STICKY_MESSAGE_KEY);
                    }
                    event.setCancelled(true);
                }
            } catch (FileNotFoundException e) {
                plugin.getLogger().log(Level.WARNING, "An error occurred while attempting to fetch " + book + " during PlayerRespawnEvent", e);
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
        if (player.hasPermission(STICKY_PERMISSION_KEY)) {
            return;
        }
        for (String book : plugin.getConfiguration().getAvailableBooks()) {
            try {
                if (!plugin.getConfiguration().getBookConfig(book).getBoolean("sticky") && event.getInventory().getType() != InventoryType.MERCHANT) {
                    return;
                }
                if (event.getCurrentItem().getItemMeta().equals(plugin.getConfiguration().getBookItem(book).getItemMeta())) {
                    if (event.getInventory().getType() != InventoryType.MERCHANT) {
                        if (STICKY_KEY != null && !STICKY_MESSAGE_KEY.isEmpty()) {
                            player.sendMessage(STICKY_MESSAGE_KEY);
                        }
                        event.setCancelled(true);
                    } else if (event.getInventory().getType() == InventoryType.MERCHANT && !plugin.getConfig().getBoolean(VILLAGER_KEY)) {
                        if (VILLAGER_MESSAGE_KEY != null && !VILLAGER_MESSAGE_KEY.isEmpty()) {
                            player.sendMessage(VILLAGER_MESSAGE_KEY);
                        }
                        event.setCancelled(true);
                    }
                }
            } catch (FileNotFoundException e) {
                plugin.getLogger().log(Level.WARNING, "An error occurred while attempting to fetch " + book + " during PlayerRespawnEvent", e);
            }
        }
    }
}
