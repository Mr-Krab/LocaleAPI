/*
 * LocaleAPI - Locale API for Sponge plugins.
 * Copyright (C) 2019 Mr_Krab
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LocaleAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package mr_krab.localeapi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import com.google.inject.Inject;

import mr_krab.localeapi.utils.LocaleAPI;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

@Plugin(id = "localeapi",
		name = "LocaleAPI",
		version = "1.0",
		description = "Locale API for Sponge plugins.",
		authors = "Mr_Krab")
public class LocaleAPIMain {

	@Inject
	@DefaultConfig(sharedRoot = false)
	private Path defaultConfig;
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;
	@Inject
	@ConfigDir(sharedRoot = false)
	private File configFile;
	@Inject
	@DefaultConfig(sharedRoot = false)
	private ConfigurationLoader<CommentedConfigurationNode> configLoader;
	private CommentedConfigurationNode rootNode;
	@Inject
	private static Logger logger;
	
	private static LocaleAPIMain instance;
	private static LocaleAPI api;

	public static LocaleAPIMain getInstance() {
		return instance;
	}

	public Logger getLogger() {
		return logger;
	}

	public CommentedConfigurationNode getRootNode() {
		return rootNode;
	}

	public String getTablePrefix() {
		return rootNode.getNode("MySQL", "DataBasePrefix").getString();
	}

	public Game getGame() {
		return Sponge.getGame();
	}

	public File getConfigFile() {
		return configFile;
	}

	public Path getConfigDir() {
		return configDir;
	}

	public LocaleAPI getAPI() {
		return api;
	}

	@Listener
	public void onServerPreInit(GamePreInitializationEvent event) throws ObjectMappingException, IOException {
		instance = this;
		logger = (Logger) LoggerFactory.getLogger("LocaleAPI");
		configLoader = HoconConfigurationLoader.builder().setPath(configDir.resolve("config.conf")).build();
		rootNode = configLoader.load();
		if(rootNode.getNode("DefaultLocale").isVirtual()) {
			rootNode.getNode("DefaultLocale").setComment("The default language for all plugins that use this API.");
			rootNode.getNode("DefaultLocale").setValue("en-US");
			configLoader.save(rootNode);
		}
		api = new LocaleAPI(instance);
	}

	@Listener
	public void onReload(GameReloadEvent event) throws IOException, ObjectMappingException {
		reload();
	}

	public void reload() throws ObjectMappingException, IOException {
		rootNode = configLoader.load();
	}
}