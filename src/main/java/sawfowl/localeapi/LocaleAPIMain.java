/*
 * LocaleAPI - Locale API for Sponge plugins.
 * Copyright (C) 2019 - 2022 SawFowl
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

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import com.google.inject.Inject;

import sawfowl.localeapi.api.LocaleAPI;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.event.LocaleServiseEvent;

@Plugin("localeapi")
public class LocaleAPIMain {

	private static PluginContainer container;
	private Logger logger;
	private LocaleService localeService;
	private Cause cause;
	private Map<String, Long> updated = new HashMap<String, Long>();

	@Inject
	public LocaleAPIMain(PluginContainer pluginContainer, @ConfigDir(sharedRoot = false) Path configDirectory) {
		container = pluginContainer;
		logger = LogManager.getLogger("LocaleAPI");
		localeService = new LocaleAPI(logger, configDirectory);
		cause = Cause.of(EventContext.builder().add(EventContextKeys.PLUGIN, pluginContainer).build(), pluginContainer);
	}

	@Listener
	public void onConstruct(ConstructPluginEvent event) {
		class ConstructEvent extends AbstractEvent implements LocaleServiseEvent.Construct {
			@Override
			public Cause cause() {
				return cause;
			}
			@Override
			public LocaleService getLocaleService() {
				return localeService;
			}
		}
		LocaleServiseEvent.Construct constructEvent = new ConstructEvent();
		Sponge.eventManager().post(constructEvent);
	}

	@Listener
	public void onStarted(StartedEngineEvent<Server> event) {
		class StartedEvent extends AbstractEvent implements LocaleServiseEvent.Started {
			@Override
			public Cause cause() {
				return cause;
			}
			@Override
			public LocaleService getLocaleService() {
				return localeService;
			}
		}
		LocaleServiseEvent.Started startedEvent = new StartedEvent();
		Sponge.eventManager().post(startedEvent);
	}

	/**
	 * This method is deprecated and will be removed in the future. </br>
	 * To get the plugin API, you need to implement the `EventForGetLocaleServise` interface. </br>
	 * See documentation.
	 */
	@Deprecated
	public LocaleService getAPI() {
		return localeService;
	}

	public static PluginContainer getPluginContainer() {
		return container;
	}

	public Map<String, Long> getUpdated() {
		return updated;
	}

}