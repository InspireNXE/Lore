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
package com.almuradev.lore.command;

import java.util.logging.Level;

import com.almuradev.lore.LorePlugin;
import com.almuradev.lore.util.VaultUtil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class LoreCommands implements CommandExecutor{
	private final LorePlugin plugin;

	public LoreCommands(LorePlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("lore")) {
			Player player = null;
			if (sender instanceof Player) {
				player = (Player) sender;
			}

			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("set")) {
					if (VaultUtil.hasPermission(player.getName(), player.getWorld().getName(), "lore.set")) {
						if (player.getItemInHand().getItemMeta() instanceof BookMeta) {
							BookMeta meta = (BookMeta) player.getItemInHand().getItemMeta();

							if (meta.getPages() != null && !meta.getPages().isEmpty()) {
								plugin.getConfiguration().setBookAuthor(meta.getAuthor());
								plugin.getConfiguration().setBookTitle(meta.getTitle());
								plugin.getConfiguration().setBookContent(meta.getPages());

								plugin.getConfiguration().save();
								player.sendMessage("Book saved to config.yml.");
							} else {
								player.sendMessage("Unable to save book to config.yml, null book contents.");
							}
						} else {
							player.sendMessage("Item in hand must be the book you want to set.");
						}
					} else {
						player.sendMessage("Insufficient permissions to use that command.");
					}
				} else if (VaultUtil.hasPermission(player.getName(), player.getWorld().getName(), "lore.give") && args[0].equalsIgnoreCase("give")) {
					Player target = Bukkit.getPlayerExact(args[1]);

					if (target != null) {
						// Do some magic
						ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
						BookMeta meta = (BookMeta) book.getItemMeta();

						meta.setAuthor(plugin.getConfiguration().getBookAuthor());
						meta.setTitle(plugin.getConfiguration().getBookTitle());
						meta.setPages(plugin.getConfiguration().getBookContent());
						book.setItemMeta(meta);

						target.getInventory().addItem(book);
					} else {
						sender.sendMessage("That player is not online.");
					}
				}
				return true;
			} else if (VaultUtil.hasPermission(player.getName(), player.getWorld().getName(), "lore.relead") && args[0].equalsIgnoreCase("reload")) {
				plugin.getConfiguration().init();
				plugin.getLogger().log(Level.INFO, "Reloaded settings from config.yml.");
			}
		}
		return false;
	}
}
