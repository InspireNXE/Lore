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

public class LoreCommands implements CommandExecutor {
	private final LorePlugin plugin;

	public LoreCommands(LorePlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("lore")) {
			Player player = null;
			Boolean isPlayer = false;

			if (sender instanceof Player) {
				isPlayer = true;
				player = (Player) sender;
			}

			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("give")) {
					if (isPlayer) {
						if (VaultUtil.hasPermission(player.getName(), player.getWorld().getName(), "lore.command.give")) {
							Player target = Bukkit.getPlayerExact(args[1]);

							if (target != null) {
								ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
								BookMeta meta = (BookMeta) book.getItemMeta();

								// Set the BookMeta's details as the ones from the config.yml that was last loaded.
								meta.setAuthor(plugin.getConfiguration().getBookAuthor());
								meta.setTitle(plugin.getConfiguration().getBookTitle());
								meta.setPages(plugin.getConfiguration().getBookContent());

								// Add the BookMeta to the Book and give it to the player.
								book.setItemMeta(meta);
								target.getInventory().addItem(book);
								sender.sendMessage("A Lore book has been given to " + target.getName());
							} else {
								sender.sendMessage("Unable to give specified player a Lore book. Is the player online?");
							}
						}
					} else {
						Player target = Bukkit.getPlayerExact(args[1]);

						if (target != null) {
							ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
							BookMeta meta = (BookMeta) book.getItemMeta();

							// Set the BookMeta's details as the ones from the config.yml that was last loaded.
							meta.setAuthor(plugin.getConfiguration().getBookAuthor());
							meta.setTitle(plugin.getConfiguration().getBookTitle());
							meta.setPages(plugin.getConfiguration().getBookContent());

							// Add the BookMeta to the Book and give it to the player.
							book.setItemMeta(meta);
							target.getInventory().addItem(book);
							sender.sendMessage("A Lore book has been given to " + target.getName());
						} else {
							sender.sendMessage("Unable to give specified player a Lore book. Is the player online?");
						}
					}
				} else if (args[0].equalsIgnoreCase("reload")) {
					if (isPlayer) {
						if (VaultUtil.hasPermission(player.getName(), player.getWorld().getName(), "lore.command.reload")) {
							plugin.getConfiguration().init();
						}
					} else {
						plugin.getConfiguration().init();
					}
				} else if (args[0].equalsIgnoreCase("set")) {
					if (isPlayer) {
						if (VaultUtil.hasPermission(player.getName(), player.getWorld().getName(), "lore.command.set")) {
							if (player.getItemInHand().getItemMeta() instanceof BookMeta) {
								BookMeta meta = (BookMeta) player.getItemInHand().getItemMeta();

								// Make sure the page content isn't null
								if (meta.getPages() != null && !meta.getPages().isEmpty()) {
									// Set the configuration values as the in-hand book's details.
									plugin.getConfiguration().setBookAuthor(meta.getAuthor());
									plugin.getConfiguration().setBookTitle(meta.getTitle());
									plugin.getConfiguration().setBookContent(meta.getPages());

									// Save save the meta to the config.yml
									plugin.getConfiguration().save();
									player.sendMessage("Book saved to config.yml.");
								} else {
									player.sendMessage("Unable to save book to config.yml, page content is null!");
								}
							}
						}
					} else {
						sender.sendMessage("A player must perform this command!");
					}
				}
				return true;
			}
		}
		return false;
	}
}
