/*
 * LocaleAPI - Locale API for Sponge plugins.
 * Copyright (C) 2019 - 2023 SawFowl
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package sawfowl.localeapi;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterBuilderEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import com.google.inject.Inject;

import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.Text;
import sawfowl.localeapi.apiclasses.TextImpl;
import sawfowl.localeapi.event.LocaleServiseEvent;

@Plugin("localeapi")
public class LocaleAPI {

	private static PluginContainer container;
	private Logger logger;
	private LocaleService localeService;
	private Cause cause;
	private Map<String, Long> updated = new HashMap<String, Long>();

	@Inject
	public LocaleAPI(PluginContainer pluginContainer, @ConfigDir(sharedRoot = false) Path configDirectory) {
		container = pluginContainer;
		logger = LogManager.getLogger("LocaleAPI");
		localeService = new API(logger, configDirectory);
		cause = Cause.of(EventContext.builder().add(EventContextKeys.PLUGIN, pluginContainer).build(), pluginContainer);
		if(!configDirectory.toFile().exists()) configDirectory.toFile().mkdir();
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

	@Listener(order = Order.FIRST)
	public void registerBuilders(RegisterBuilderEvent event) {
		event.register(Text.Builder.class, new Supplier<Text.Builder>() {
			@Override
			public Text.Builder get() {
				return new TextImpl().builder();
			}
		});
	}

	public static PluginContainer getPluginContainer() {
		return container;
	}

	public Map<String, Long> getUpdated() {
		return updated;
	}

}