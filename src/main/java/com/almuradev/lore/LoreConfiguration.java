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
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class LoreConfiguration {
	private final LorePlugin plugin;

	public LoreConfiguration(LorePlugin plugin) {
		this.plugin = plugin;
	}

	public void init() {
		// Verify that our config.yml exists
		if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
			plugin.saveDefaultConfig();
		}

		if (!new File(plugin.getDataFolder() + "/books/").exists()) {
			try {
				new File(plugin.getDataFolder() + "/books/").mkdir();
			} catch (Exception e) {
				plugin.getLogger().severe("Unable to create 'books' folder in basedir/plugins/Lore");
				if (debugMode()) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean debugMode() {
		return plugin.getConfig().getBoolean("debug");
	}

	public String getJoinMessage() {
		if (!plugin.getConfig().getString("join.message").isEmpty()) {
			return plugin.getConfig().getString("join.message");
		}
		return "";
	}

	public List<String> getJoinBooks() {
		return plugin.getConfig().getStringList("join.books");
	}

	public String getRespawnMessage() {
		if (!plugin.getConfig().getString("respawn.message").isEmpty()) {
			return plugin.getConfig().getString("respawn.message");
		}
		return "";
	}

	public List<String> getRespawnBooks() {
		return plugin.getConfig().getStringList("respawn.books");
	}

	public ItemStack getBook(String name) {
		YamlConfiguration bookConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + "/books/" + name + ".yml"));
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
		BookMeta meta = (BookMeta) book.getItemMeta();

		if (verifyBook(name)) {
			if (bookConfig.getStringList("pages") != null && !bookConfig.getStringList("pages").isEmpty()) {
				meta.setAuthor(bookConfig.getString("author"));
				meta.setTitle(bookConfig.getString("title"));
				meta.setPages(bookConfig.getStringList("pages"));
				book.setItemMeta(meta);
			} else {
				plugin.getLogger().severe("Unable to obtain " + name + "\'s pages.");
			}
		}
		return book;
	}

	public void createBook(BookMeta meta) {
		// Make sure the book file exists
		File bookFile = new File(plugin.getDataFolder() + "/books/" + meta.getTitle() + ".yml");
		if (!verifyBook(meta.getTitle())) {
			try {
				bookFile.createNewFile();
			} catch (IOException e) {
				plugin.getLogger().severe("Unable to create " + meta.getTitle() + ".yml!");
				if (debugMode()) {
					e.printStackTrace();
				}
			}
		}

		// Get the configuration file for the book and add all the stuff
		YamlConfiguration bookConfig = YamlConfiguration.loadConfiguration(bookFile);
		bookConfig.set("author", meta.getAuthor());
		bookConfig.set("title", meta.getTitle());
		bookConfig.set("pages", meta.getPages());
		try {
			bookConfig.save(bookFile);
		} catch (IOException e) {
			plugin.getLogger().severe("Unable to save file for " + meta.getTitle() + ".");
			if (debugMode()) {
				e.printStackTrace();
			}
		}
	}

	public void removeBook(String name) {
		if (verifyBook(name)) {
			try {
				new File(plugin.getDataFolder() + "/books/" + name + ".yml").delete();
			} catch (Exception e) {
				plugin.getLogger().severe("Unable to delete " + name + ".yml!");
				if (debugMode()) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean addJoinBook(String name) {
		List<String> joinBookList = plugin.getConfig().getConfigurationSection("join").getStringList("books");
		if (!joinBookList.contains(name)) {
			joinBookList.add(name);
			plugin.getConfig().set("join.books", joinBookList);
			plugin.getLogger().info(name + " has been added to the list of join books.");
			plugin.saveConfig();
			return true;
		}
		return false;
	}

	public boolean addRespawnBook(String name) {
		List<String> respawnBookList = plugin.getConfig().getStringList("respawn.books");
		if (!respawnBookList.contains(name)) {
			respawnBookList.add(name);
			plugin.getConfig().set("respawn.books", respawnBookList);
			plugin.getConfig().getConfigurationSection("respawn").getStringList("books").add(name);
			plugin.getLogger().info(name + " has been added to the list of respawn books.");
			plugin.saveConfig();
			return true;
		}
		return false;
	}

	public boolean removeJoinBook(String name) {
		List<String> joinBookList = plugin.getConfig().getStringList("join.books");
		if (joinBookList.contains(name)) {
			joinBookList.remove(name);
			plugin.getConfig().set("join.books", joinBookList);
			plugin.getLogger().info(name + " has been removed from the list of join books.");
			plugin.saveConfig();
			return true;
		}
		return false;
	}

	public boolean removeRespawnBook(String name) {
		List<String> respawnBookList = plugin.getConfig().getStringList("respawn.books");
		if (respawnBookList.contains(name)) {
			respawnBookList.remove(name);
			plugin.getConfig().set("respawn.books", respawnBookList);
			plugin.getLogger().info(name + " has been removed from the list of respawn books.");
			plugin.saveConfig();
			return true;
		}
		return false;
	}

	public boolean verifyBook(String name) {
		if (new File(plugin.getDataFolder() + "/books/" + name + ".yml").exists()) {
			if (debugMode()) {
				plugin.getLogger().info(plugin.getDataFolder() + "/books/" + name + ".yml");
			}
			return true;
		}
		return false;
	}
}
