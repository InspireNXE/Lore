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
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
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
    private void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (player.hasPlayedBefore()) {
            return;
        }
        if (!player.hasPermission(JOIN_PERMISSION_KEY)) {
            return;
        }
        for (Map.Entry<String, ItemStack> entry : plugin.getConfiguration().getMap().entrySet()) {
            final String name = entry.getKey();
            final ItemStack item = entry.getValue();
            try {
                if (player.getInventory().contains(item)) {
                    return;
                }
                if (plugin.getConfiguration().getConfig(name).getBoolean(JOIN_KEY)) {
                    player.getInventory().addItem(item);
                    if (JOIN_MESSAGE_KEY != null && !JOIN_MESSAGE_KEY.isEmpty()) {
                        player.sendMessage(JOIN_MESSAGE_KEY);
                    }
                }
            } catch (FileNotFoundException e) {
                plugin.getLogger().log(Level.WARNING, "An error occurred while attempting to fetch " + ChatColor.GREEN + name.toLowerCase() + ChatColor.RESET + " during PlayerJoinEvent", e);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        if (!player.hasPermission(RESPAWN_PERMISSION_KEY)) {
            return;
        }
        for (Map.Entry<String, ItemStack> entry : plugin.getConfiguration().getMap().entrySet()) {
            final String name = entry.getKey();
            final ItemStack item = entry.getValue();
            try {
                if (player.getInventory().contains(item)) {
                    return;
                }
                if (plugin.getConfiguration().getConfig(name).getBoolean(RESPAWN_KEY)) {
                    player.getInventory().addItem(item);
                    if (RESPAWN_MESSAGE_KEY != null && !RESPAWN_MESSAGE_KEY.isEmpty()) {
                        player.sendMessage(RESPAWN_MESSAGE_KEY);
                    }
                }
            } catch (FileNotFoundException e) {
                plugin.getLogger().log(Level.WARNING, "An error occurred while attempting to fetch " + ChatColor.GREEN + name.toLowerCase() + ChatColor.RESET + " during PlayerRespawnEvent", e);
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    private void onPlayerDropItem(PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        if (event.getItemDrop() == null) {
            return;
        }
        if (!(event.getItemDrop().getItemStack().getItemMeta() instanceof BookMeta)) {
            return;
        }
        if (player.hasPermission(STICKY_PERMISSION_KEY)) {
            return;
        }
        for (Map.Entry<String, ItemStack> entry : plugin.getConfiguration().getMap().entrySet()) {
            final String name = entry.getKey();
            try {
                if (!plugin.getConfiguration().getConfig(name).getBoolean(STICKY_KEY)) {
                    continue;
                }
                final ItemStack item = entry.getValue();
                if (item.getItemMeta().equals(event.getItemDrop().getItemStack().getItemMeta())) {
                    if (STICKY_MESSAGE_KEY != null && !STICKY_MESSAGE_KEY.isEmpty()) {
                        player.sendMessage(STICKY_MESSAGE_KEY);
                    }
                    event.setCancelled(true);
                    break;
                }
            } catch (FileNotFoundException e) {
                plugin.getLogger().log(Level.WARNING, "An error occurred while attempting to fetch " + ChatColor.GREEN + name.toLowerCase() + ChatColor.RESET + " during PlayerDropItemEvent", e);
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    private void onInventoryClick(InventoryClickEvent event) {
        final Player player = event.getWhoClicked() instanceof Player ? (Player) event.getWhoClicked() : null;
        if (player == null) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        if (!(event.getCurrentItem().getItemMeta() instanceof BookMeta)) {
            return;
        }
        if (player.hasPermission(STICKY_PERMISSION_KEY)) {
            return;
        }
        final InventoryType type = event.getInventory().getType();
        if (type != InventoryType.CHEST && type != InventoryType.DISPENSER && type != InventoryType.DROPPER && type != InventoryType.ENDER_CHEST && type != InventoryType.HOPPER && type != InventoryType.MERCHANT) {
            return;
        }
        for (Map.Entry<String, ItemStack> entry : plugin.getConfiguration().getMap().entrySet()) {
            final String name = entry.getKey();
            try {
                if (!plugin.getConfiguration().getConfig(name).getBoolean(STICKY_KEY)) {
                    continue;
                }
                final ItemStack item = entry.getValue();
                if (item.getItemMeta().equals(event.getCurrentItem().getItemMeta())) {
                    if (type == InventoryType.MERCHANT && !plugin.getConfig().getBoolean(VILLAGER_KEY)) {
                        if (VILLAGER_MESSAGE_KEY != null && !VILLAGER_MESSAGE_KEY.isEmpty()) {
                            player.sendMessage(VILLAGER_MESSAGE_KEY);
                        }
                    } else {
                        if (STICKY_MESSAGE_KEY != null && !STICKY_MESSAGE_KEY.isEmpty()) {
                            player.sendMessage(STICKY_MESSAGE_KEY);
                        }
                    }
                    event.setCancelled(true);
                }
            } catch (FileNotFoundException e) {
                plugin.getLogger().log(Level.WARNING, "An error occurred while attempting to fetch " + ChatColor.GREEN + name.toLowerCase() + ChatColor.RESET + " during InventoryClickEvent", e);
            }
        }
    }
}
