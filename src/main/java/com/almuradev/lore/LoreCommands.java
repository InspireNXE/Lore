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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class LoreCommands implements CommandExecutor {
    private final LorePlugin plugin;
    private final Path booksPath;
    private final String PERMISSION_MESSAGE_KEY;
    private final String CREATE_PERMISSION_KEY = "lore.command.create";
    private final String GIVE_PERMISSION_KEY = "lore.command.give";
    private final String JOIN_PERMISSION_KEY = "lore.command.join";
    private final String LIST_PERMISSION_KEY = "lore.command.list";
    private final String REMOVE_PERMISSION_KEY = "lore.command.remove";
    private final String RESPAWN_PERMISSION_KEY = "lore.command.respawn";
    private final String STICKY_PERMISSION_KEY = "lore.command.sticky";
    private final String UNSIGN_PERMISSION_KEY = "lore.command.unsign";
    private final String UPDATE_PERMISSION_KEY = "lore.command.update";

    public LoreCommands(LorePlugin plugin) {
        this.plugin = plugin;
        booksPath = Paths.get(plugin.getDataFolder() + File.separator + "books");
        PERMISSION_MESSAGE_KEY = plugin.getConfig().getString("messages.permission");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final Player player = sender instanceof Player ? (Player) sender : null;

        if (args.length == 0) {
            return false;
        }
        switch (args[0].toUpperCase()) {
            case "CREATE":
                if (player == null) {
                    sender.sendMessage("Only players can perform this command.");
                    return true;
                }
                if (args.length < 2) {
                    return false;
                }
                if (!player.hasPermission(CREATE_PERMISSION_KEY)) {
                    if (PERMISSION_MESSAGE_KEY != null && !PERMISSION_MESSAGE_KEY.isEmpty()) {
                        sender.sendMessage(PERMISSION_MESSAGE_KEY);
                    }
                    return true;
                }
                final ItemStack createItem = player.getItemInHand();
                final ItemMeta createMeta = createItem.getItemMeta();
                if (!(createMeta instanceof BookMeta) || createItem.getType() != Material.WRITTEN_BOOK) {
                    sender.sendMessage("You must hold a signed book with this command.");
                } else {
                    try {
                        plugin.getConfiguration().create(args[1], (BookMeta) createMeta);
                        sender.sendMessage(ChatColor.GREEN + args[1].toLowerCase() + ChatColor.RESET + " was added to the library.");
                    } catch (FileAlreadyExistsException e) {
                        sender.sendMessage(ChatColor.GREEN + args[1].toLowerCase() + ChatColor.RESET + " already exists in the library.");
                    } catch (IOException e) {
                        sender.sendMessage("There was an issue in adding this book to the library, please inform your server administrator.");
                        plugin.getLogger().log(Level.SEVERE, "Unable to create " + ChatColor.GREEN + args[1] + ".yml" + ChatColor.RESET, e);
                    }
                }
                return true;

            case "GIVE":
                if (args.length < 3) {
                    return false;
                }
                if (player != null && !player.hasPermission(GIVE_PERMISSION_KEY)) {
                    if (PERMISSION_MESSAGE_KEY != null && !PERMISSION_MESSAGE_KEY.isEmpty()) {
                        sender.sendMessage(PERMISSION_MESSAGE_KEY);
                    }
                    return true;
                }
                final Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.GRAY + args[1].toLowerCase() + ChatColor.RESET + " does not appear to be online.");
                    return true;
                }
                try {
                    final ItemStack item = plugin.getConfiguration().getMap().get(args[2].toLowerCase());
                    if (item == null) {
                        sender.sendMessage(ChatColor.GREEN + args[2].toLowerCase() + ChatColor.RESET + " does not exist in the library.");
                        return true;
                    } else if (target.getInventory().contains(item)) {
                        sender.sendMessage(ChatColor.RED + target.getName() + ChatColor.RESET + " already has a copy of " + ChatColor.GREEN + args[2].toLowerCase() + ChatColor.RESET);
                        return true;
                    } else {
                        target.getInventory().addItem(item);
                        if (sender != target) {
                            target.sendMessage("You've been given a copy of " + ChatColor.GREEN + args[2].toLowerCase() + ChatColor.RESET);
                            sender.sendMessage(ChatColor.RED + target.getName() + ChatColor.RESET + " was given a copy of " + ChatColor.GREEN + args[2].toLowerCase() + ChatColor.RESET);
                        } else {
                            sender.sendMessage("You gave yourself a copy of " + ChatColor.GREEN + args[2].toLowerCase() + ChatColor.RESET);
                        }
                    }
                } catch (NullPointerException e) {
                    plugin.getLogger().log(Level.WARNING, "Unable to obtain ItemStack for " + ChatColor.GREEN + args[2].toLowerCase() + ".yml" + ChatColor.RESET, e);
                }
                return true;

            case "JOIN":
                if (args.length < 3) {
                    return false;
                }
                if (player != null && !player.hasPermission(JOIN_PERMISSION_KEY)) {
                    if (PERMISSION_MESSAGE_KEY != null && !PERMISSION_MESSAGE_KEY.isEmpty()) {
                        sender.sendMessage(PERMISSION_MESSAGE_KEY);
                    }
                    return true;
                }
                try {
                    final YamlConfiguration config = plugin.getConfiguration().getConfig(args[1]);
                    config.set("join", Boolean.parseBoolean(args[2]));
                    config.save(Paths.get(booksPath + File.separator + args[1].toLowerCase() + ".yml").toFile());
                    sender.sendMessage(ChatColor.GREEN + args[1].toLowerCase() + ChatColor.RESET + " has had the join flag set to " + ChatColor.YELLOW + Boolean.parseBoolean(args[2]) + ChatColor.RESET);
                } catch (NoSuchFileException e) {
                    sender.sendMessage(ChatColor.GREEN + args[1].toLowerCase() + ChatColor.RESET + " does not exist in the library.");
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "An error occurred when attempting to save " + ChatColor.GREEN + args[1] + ".yml" + ChatColor.RESET, e);
                    sender.sendMessage("An error occurred when attempting to save " + ChatColor.GREEN + args[1].toLowerCase() + ".yml" + ChatColor.RESET + ", please inform your server administrator.");
                }
                return true;

            case "LIST":
                if (player != null && !player.hasPermission(LIST_PERMISSION_KEY)) {
                    if (PERMISSION_MESSAGE_KEY != null && !PERMISSION_MESSAGE_KEY.isEmpty()) {
                        sender.sendMessage(PERMISSION_MESSAGE_KEY);
                    }
                    return true;
                }
                final SortedSet<String> names = new TreeSet<>(plugin.getConfiguration().getMap().keySet());
                if (names.isEmpty()) {
                    sender.sendMessage("No books are registered in Lore.");
                } else {
                    sender.sendMessage("Registered books in Lore: " + names.toString().replace("[", ChatColor.GREEN + "").replace(", ", ChatColor.RESET + ", " + ChatColor.GREEN).replace("]", ChatColor.RESET + "").trim());
                }
                return true;

            case "REMOVE":
                if (args.length < 2) {
                    return false;
                }
                if (player != null && !player.hasPermission(REMOVE_PERMISSION_KEY)) {
                    if (PERMISSION_MESSAGE_KEY != null && !PERMISSION_MESSAGE_KEY.isEmpty()) {
                        sender.sendMessage(PERMISSION_MESSAGE_KEY);
                    }
                    return true;
                }
                try {
                    plugin.getConfiguration().delete(args[1]);
                    sender.sendMessage(ChatColor.GREEN + args[1].toLowerCase() + ChatColor.RESET + " has been removed from Lore.");
                } catch (NoSuchFileException e) {
                    sender.sendMessage(ChatColor.GREEN + args[1].toLowerCase() + ChatColor.RESET + " does not exist in the library.");
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Unable to delete " + ChatColor.GREEN + args[1].toLowerCase() + ChatColor.RESET, e);
                }
                return true;

            case "RESPAWN":
                if (args.length < 3) {
                    return false;
                }
                if (player != null && !player.hasPermission(RESPAWN_PERMISSION_KEY)) {
                    if (PERMISSION_MESSAGE_KEY != null && !PERMISSION_MESSAGE_KEY.isEmpty()) {
                        sender.sendMessage(PERMISSION_MESSAGE_KEY);
                    }
                    return true;
                }
                try {
                    final YamlConfiguration config = plugin.getConfiguration().getConfig(args[1]);
                    config.set("respawn", Boolean.parseBoolean(args[2]));
                    config.save(Paths.get(booksPath + File.separator + args[1].toLowerCase() + ".yml").toFile());
                    sender.sendMessage(ChatColor.GREEN + args[1].toLowerCase() + ChatColor.RESET + " has had the respawn flag set to " + ChatColor.YELLOW + Boolean.parseBoolean(args[2]) + ChatColor.RESET);
                } catch (NoSuchFileException e) {
                    sender.sendMessage(ChatColor.GREEN + args[1].toLowerCase() + ChatColor.RESET + " does not exist in the library.");
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "An error occurred when attempting to save " + ChatColor.GREEN + args[1].toLowerCase() + ".yml" + ChatColor.RESET, e);
                    sender.sendMessage("An error occurred when attempting to save " + ChatColor.GREEN + args[1].toLowerCase() + ".yml" + ChatColor.RESET + ", please inform your server administrator.");
                }
                return true;

            case "STICKY":
                if (args.length < 3) {
                    return false;
                }
                if (player != null && !player.hasPermission(STICKY_PERMISSION_KEY)) {
                    if (PERMISSION_MESSAGE_KEY != null && !PERMISSION_MESSAGE_KEY.isEmpty()) {
                        sender.sendMessage(PERMISSION_MESSAGE_KEY);
                    }
                    return true;
                }
                try {
                    final YamlConfiguration config = plugin.getConfiguration().getConfig(args[1]);
                    config.set("sticky", Boolean.parseBoolean(args[2]));
                    config.save(Paths.get(booksPath + File.separator + args[1].toLowerCase() + ".yml").toFile());
                    sender.sendMessage(ChatColor.GREEN + args[1].toLowerCase() + ChatColor.RESET + " has had the sticky flag set to " + ChatColor.YELLOW + Boolean.parseBoolean(args[2]) + ChatColor.RESET);
                } catch (NoSuchFileException e) {
                    sender.sendMessage(ChatColor.GREEN + args[1].toLowerCase() + ChatColor.RESET + " does not exist in the library.");
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "An error occurred when attempting to save " + ChatColor.GREEN + args[1].toLowerCase() + ".yml" + ChatColor.RESET, e);
                    sender.sendMessage("An error occurred when attempting to save " + ChatColor.GREEN + args[1].toLowerCase() + ".yml" + ChatColor.RESET + ", please inform your server administrator.");
                }
                return true;

            case "UNSIGN":
                if (player == null) {
                    sender.sendMessage("Only players can perform this command.");
                    return true;
                }
                if (!player.hasPermission(UNSIGN_PERMISSION_KEY)) {
                    if (PERMISSION_MESSAGE_KEY != null && !PERMISSION_MESSAGE_KEY.isEmpty()) {
                        sender.sendMessage(PERMISSION_MESSAGE_KEY);
                    }
                    return true;
                }
                final ItemStack signedItem = player.getItemInHand();
                final ItemMeta signedMeta = signedItem.getItemMeta();
                if (!(signedMeta instanceof BookMeta) || signedItem.getType() != Material.WRITTEN_BOOK) {
                    sender.sendMessage("You must be holding a signed book to use this command!");
                } else {
                    final ItemStack unsigned = new ItemStack(Material.BOOK_AND_QUILL);
                    unsigned.setItemMeta(signedMeta);
                    player.setItemInHand(unsigned);
                    sender.sendMessage("You now are holding an unsigned copy of " + ChatColor.GREEN + ((BookMeta) signedMeta).getTitle() + ChatColor.RESET);
                }
                return true;

            case "UPDATE":
                if (player == null) {
                    sender.sendMessage("Only players can perform this command.");
                    return true;
                }
                if (args.length < 2) {
                    return false;
                }
                if (!player.hasPermission(UPDATE_PERMISSION_KEY)) {
                    if (PERMISSION_MESSAGE_KEY != null && !PERMISSION_MESSAGE_KEY.isEmpty()) {
                        sender.sendMessage(PERMISSION_MESSAGE_KEY);
                    }
                    return true;
                }
                final ItemStack updateItem = player.getItemInHand();
                final ItemMeta updateMeta = updateItem.getItemMeta();
                if (!(updateMeta instanceof BookMeta) || updateItem.getType() != Material.WRITTEN_BOOK) {
                    sender.sendMessage("You must be holding a signed book to use this command!");
                } else {
                    try {
                        plugin.getConfiguration().delete(args[1]);
                        plugin.getConfiguration().create(args[1], (BookMeta) updateMeta);
                        sender.sendMessage(ChatColor.GREEN + args[1].toLowerCase() + ChatColor.RESET + " was updated.");
                    } catch (NoSuchFileException e) {
                        sender.sendMessage(ChatColor.GREEN + args[1].toLowerCase() + ChatColor.RESET + " does not exist in the library.");
                    } catch (IOException e) {
                        sender.sendMessage("There was an issue updating this book, please inform your server administrator.");
                        plugin.getLogger().log(Level.SEVERE, "Unable to update " + ChatColor.GREEN + args[1].toLowerCase() + ".yml" + ChatColor.RESET, e);
                    }
                }
                return true;
        }
        return false;
    }
}
