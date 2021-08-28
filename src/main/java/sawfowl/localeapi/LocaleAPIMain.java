/*
 * LocaleAPI - Locale API for Sponge plugins.
 * Copyright (C) 2019 - 2021 Mr_Krab
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
package sawfowl.localeapi;

import java.io.File;
import java.nio.file.Path;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import com.google.inject.Inject;

import sawfowl.localeapi.api.LocaleAPI;
import sawfowl.localeapi.api.LocaleService;

@Plugin("localeapi")
public class LocaleAPIMain {

	private Path configDir;
	private File configFile;
	private Logger logger;

	private PluginContainer pluginContainer;
	private LocaleAPIMain instance;
	private LocaleService api;
	private ObjectMapper.Factory factory;
	private TypeSerializerCollection child;
	private ConfigurationOptions options;

	@Inject
	public LocaleAPIMain(PluginContainer pluginContainer, @ConfigDir(sharedRoot = false) Path configDirectory) {
		this.configDir = configDirectory;
		this.pluginContainer = pluginContainer;
		instance = this;
		logger = LogManager.getLogger("LocaleAPI");
		this.configFile = configDir.toFile();
		factory = ObjectMapper.factoryBuilder().addNodeResolver(NodeResolver.onlyWithSetting()).build();
		child = TypeSerializerCollection.defaults().childBuilder().registerAnnotatedObjects(factory).build();
		options = ConfigurationOptions.defaults().serializers(child);
		api = new LocaleAPI(instance);
	}

	public Logger getLogger() {
		return logger;
	}

	public File getConfigFile() {
		return configFile;
	}

	public Path getConfigDir() {
		return configDir;
	}

	public LocaleService getAPI() {
		return api;
	}

	public PluginContainer getPluginContainer() {
		return pluginContainer;
	}

	public ConfigurationOptions getConfigurationOptions() {
		return options;
	}

}
