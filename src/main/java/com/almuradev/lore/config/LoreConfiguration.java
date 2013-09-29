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
package com.almuradev.lore.config;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

import com.almuradev.lore.LorePlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class LoreConfiguration {
	private final LorePlugin plugin;
	private String bookAuthor, bookTitle, joinMessage, respawnMessage;
	private List<String> bookContent;
	private FileConfiguration config;

	public LoreConfiguration(LorePlugin plugin) {
		this.plugin = plugin;
	}

	public void init() {
		// Read in default config.yml
		if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
			plugin.saveDefaultConfig();
		}

		config = plugin.getConfig();
		bookAuthor = config.getString("author");
		bookTitle = config.getString("title");
		bookContent = config.getStringList("content");
		joinMessage = config.getString("join-message");
		respawnMessage = config.getString("respawn-message");

		if (bookContent == null || bookContent.isEmpty()) {
			plugin.getLogger().log(Level.SEVERE, "Unable to get content for Lore book. Use the '/lore set' command while holding a book to make that the Lore book!");
			bookContent.add("");
		} else {
			plugin.getLogger().log(Level.INFO, "Loaded settings from Lore's config.yml successfully.");
		}
	}

	public void save() {
		config.set("author", bookAuthor);
		config.set("title", bookTitle);
		config.set("content", bookContent);
		plugin.saveConfig();
	}

	public String getJoinMessage() {
		return joinMessage;
	}

	public String getRespawnMessage() {
		return respawnMessage;
	}

	public String getBookAuthor() {
		return bookAuthor;
	}

	public String getBookTitle() {
		return bookTitle;
	}

	public List<String> getBookContent() {
		return bookContent;
	}

	public void setBookAuthor(String bookAuthor) {
		this.bookAuthor = bookAuthor;
	}

	public void setBookTitle(String bookTitle) {
		this.bookTitle = bookTitle;
	}

	public void setBookContent(List<String> bookContent) {
		this.bookContent = bookContent;
	}

	public ItemStack getBook() {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
		BookMeta meta = (BookMeta) book.getItemMeta();

		if (plugin.getConfiguration().getBookContent() != null && !plugin.getConfiguration().getBookContent().isEmpty()) {
			meta.setAuthor(plugin.getConfiguration().getBookAuthor());
			meta.setTitle(plugin.getConfiguration().getBookTitle());
			meta.setPages(plugin.getConfiguration().getBookContent());
			book.setItemMeta(meta);
		}

		return book;
	}
}
