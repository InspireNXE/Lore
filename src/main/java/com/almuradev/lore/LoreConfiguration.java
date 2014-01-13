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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

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
        // Check if the config.yml exists, if not then create it.
        if (!Files.exists(Paths.get(plugin.getDataFolder() + File.separator + "config.yml"))) {
            plugin.saveDefaultConfig();
        }

        // Check if the books folder exists, if not then create it.
        if (!Files.exists(Paths.get(plugin.getDataFolder() + File.separator + "books"))) {
            try {
                Files.createDirectories(Paths.get(plugin.getDataFolder() + File.separator + "books"));
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "An error occurred while attempting to create a folder in Lore's plugin data folder.", e);
            }
        }
    }

    public void save(String name, BookMeta meta) throws IOException {
        Files.createFile(Paths.get(plugin.getDataFolder() + File.separator + "books" + File.separator + name + ".yml"));
        final YamlConfiguration bookConfig = getConfig(name);
        bookConfig.set("title", meta.getTitle());
        bookConfig.set("author", meta.getAuthor());
        bookConfig.set("pages", meta.getPages());
        bookConfig.set("respawn", false);
        bookConfig.set("join", false);
        bookConfig.set("sticky", false);
        bookConfig.save(Paths.get(plugin.getDataFolder() + File.separator + "books" + File.separator + name + ".yml").toFile());
    }

    public void delete(String name) throws IOException {
        Files.delete(Paths.get(plugin.getDataFolder() + File.separator + "books" + File.separator + name + ".yml"));
    }

    public YamlConfiguration getConfig(String name) throws FileNotFoundException {
        if (!Files.exists(Paths.get(plugin.getDataFolder() + File.separator + "books" + File.separator + name + ".yml"))) {
            throw new FileNotFoundException();
        }
        return YamlConfiguration.loadConfiguration(Paths.get(plugin.getDataFolder() + File.separator + "books" + File.separator + name + ".yml").toFile());
    }

    public ItemStack getItem(String name) throws NullPointerException, FileNotFoundException {
        final ItemStack item = new ItemStack(Material.WRITTEN_BOOK, 1);
        final BookMeta meta = (BookMeta) item.getItemMeta();
        meta.setTitle(getConfig(name).getString("title"));
        meta.setAuthor(getConfig(name).getString("author"));
        meta.setPages(getConfig(name).getStringList("pages"));
        item.setItemMeta(meta);
        return item;
    }

    public List<String> getAvailableBooks() {
        final List<String> availableBooksList = new ArrayList<>();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(plugin.getDataFolder() + File.separator + "books"))) {
            for (Path path : dirStream) {
                availableBooksList.add(path.getFileName().toString().replace(".yml", ""));
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "There was an issue obtaining books from Lore's plugin data folder.", e);
        }
        return availableBooksList;
    }
}
