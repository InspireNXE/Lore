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

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LorePlugin extends JavaPlugin {
	private LoreConfiguration config;

	@Override
	public void onEnable() {
		config = new LoreConfiguration(this);

		// Initialize configuration
		config.init();

		// Register listeners
		Bukkit.getServer().getPluginManager().registerEvents(new LoreListener(this), this);

		// Register commands
		getCommand("lore").setExecutor(new LoreCommands(this));

		// Are we in debug mode?
		if (config.debugMode()) {
			getLogger().info("Debug mode is enabled.");
		}
	}

	public LoreConfiguration getConfiguration() {
		return config;
	}
}
