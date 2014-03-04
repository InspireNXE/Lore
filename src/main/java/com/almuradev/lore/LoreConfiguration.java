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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class LoreConfiguration {
    private final LorePlugin plugin;
    private final Map<String, ItemStack> BOOK_MAP = new HashMap<>();
    private final Path configPath;
    private final Path booksPath;

    public LoreConfiguration(LorePlugin plugin) {
        this.plugin = plugin;
        configPath = Paths.get(plugin.getDataFolder() + File.separator + "config.yml");
        booksPath = Paths.get(plugin.getDataFolder() + File.separator + "books");
    }

    protected void init() {
        // Check if the config.yml exists, if not then create it.
        if (Files.notExists(configPath)) {
            plugin.getLogger().info("config.yml was not found, creating default.");
            plugin.saveDefaultConfig();
            plugin.getConfig().set("messages.join", "You've just received book(s) of lore.");
            plugin.getConfig().set("messages.permission", "You do not have permission to perform that command.");
            plugin.getConfig().set("messages.respawn", "You've just received book(s) of lore.");
            plugin.getConfig().set("messages.sticky", "You are not allowed to get rid of that book!");
            plugin.getConfig().set("messages.villager", "You are not allowed to trade that book to a villager!");
            plugin.saveConfig();
        }

        // Check if the books folder exists, if not then create it.
        if (Files.notExists(booksPath)) {
            try {
                Files.createDirectories(booksPath);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "An error occurred while attempting to create a folder in Lore's plugin data folder", e);
            }
        }

        // Populate BOOK_MAP
        populate();
    }

    public void populate() {
        BOOK_MAP.clear();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(booksPath)) {
            for (Path path : dirStream) {
                final String name = path.getFileName().toString().replace(".yml", "").toLowerCase();
                BOOK_MAP.put(name, getItem(name));
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "There was an issue obtaining books from Lore's plugin data folder", e);
        }
    }

    public void create(String name, BookMeta meta) throws IOException {
        final Path bookPath = Paths.get(booksPath + File.separator + name.toLowerCase() + ".yml");
        Files.createFile(bookPath);
        final YamlConfiguration config = getConfig(name.toLowerCase());
        config.set("title", meta.getTitle());
        config.set("author", meta.getAuthor());
        config.set("pages", meta.getPages());
        config.set("respawn", false);
        config.set("join", false);
        config.set("sticky", false);
        config.save(bookPath.toFile());
        BOOK_MAP.put(name.toLowerCase(), getItem(name.toLowerCase()));
    }

    public void delete(String name) throws IOException {
        Files.delete(Paths.get(booksPath + File.separator + name.toLowerCase() + ".yml"));
        BOOK_MAP.remove(name.toLowerCase());
    }

    public YamlConfiguration getConfig(String name) throws FileNotFoundException {
        final Path path = Paths.get(booksPath + File.separator + name.toLowerCase() + ".yml");
        if (Files.notExists(path)) {
            throw new FileNotFoundException();
        }
        return YamlConfiguration.loadConfiguration(path.toFile());
    }

    public ItemStack getItem(String name) throws NullPointerException, FileNotFoundException {
        final ItemStack item = new ItemStack(Material.WRITTEN_BOOK, 1);
        final BookMeta meta = (BookMeta) item.getItemMeta();
        final YamlConfiguration config = getConfig(name.toLowerCase());
        meta.setTitle(config.getString("title"));
        meta.setAuthor(config.getString("author"));
        meta.setPages(config.getStringList("pages"));
        item.setItemMeta(meta);
        return item;
    }

    public Map<String, ItemStack> getMap() {
        return Collections.unmodifiableMap(BOOK_MAP);
    }
}
