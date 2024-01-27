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
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterBuilderEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Nameable;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import com.google.inject.Inject;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;

import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.Text;
import sawfowl.localeapi.api.event.LocaleServiseEvent;
import sawfowl.localeapi.api.placeholders.Placeholders;
import sawfowl.localeapi.api.placeholders.Placeholders.DefaultPlaceholderKeys;
import sawfowl.localeapi.apiclasses.TextImpl;

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
		registerDefaultPlaceholders();
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

	private void registerDefaultPlaceholders() {
		Placeholders.register(Nameable.class, DefaultPlaceholderKeys.NAMEABLE, (text, namable, def) -> (text.replace(DefaultPlaceholderKeys.NAMEABLE, namable.name())));
		Placeholders.register(ServerPlayer.class, DefaultPlaceholderKeys.PLAYER_PING, (text, player, def) -> (text.replace(DefaultPlaceholderKeys.PLAYER_PING, player.connection().latency())));
		Placeholders.register(Entity.class, DefaultPlaceholderKeys.ENTITY_DISPLAY_NAME, (text, entity, def) -> (text.replace(DefaultPlaceholderKeys.ENTITY_DISPLAY_NAME, entity.getValue(Keys.CUSTOM_NAME).map(value -> value.get()).orElse(entity instanceof Nameable ? Component.text(((Nameable) entity).name()) : (def == null ? Component.text("n/a") : def)))));
		Placeholders.register(Entity.class, DefaultPlaceholderKeys.ENTITY_UUID, (text, entity, def) -> (text.replace(DefaultPlaceholderKeys.ENTITY_UUID, entity.uniqueId())));
		Placeholders.register(ServerWorld.class, DefaultPlaceholderKeys.WORLD, (text, world, def) -> (text.replace(DefaultPlaceholderKeys.WORLD, world.key().asString())));
		Placeholders.register(ServerLocation.class, DefaultPlaceholderKeys.LOCATION, (text, location, def) -> (text.replace(DefaultPlaceholderKeys.LOCATION, "<" + location.world().key().asString() + ">" + location.blockPosition().toString())));
		Placeholders.register(Vector3d.class, DefaultPlaceholderKeys.POSITION, (text, vector3d, def) -> (text.replace(DefaultPlaceholderKeys.POSITION, vector3d.toString())));
		Placeholders.register(Vector3i.class, DefaultPlaceholderKeys.BLOCK_POSITION, (text, vector3i, def) -> (text.replace(DefaultPlaceholderKeys.BLOCK_POSITION, vector3i.toString())));
		Placeholders.register(ServerPlayer.class, DefaultPlaceholderKeys.PLAYER_PREFIX, (text, player, def) -> (text.replace(DefaultPlaceholderKeys.PLAYER_PREFIX, player.option("prefix").orElse(""))));
		Placeholders.register(ServerPlayer.class, DefaultPlaceholderKeys.PLAYER_SUFFIX, (text, player, def) -> (text.replace(DefaultPlaceholderKeys.PLAYER_SUFFIX, player.option("suffix").orElse(""))));
		Placeholders.register(ServerPlayer.class, DefaultPlaceholderKeys.PLAYER_RANK, (text, player, def) -> (text.replace(DefaultPlaceholderKeys.PLAYER_RANK, player.option("rank").orElse(""))));
		Placeholders.register(ItemStack.class, DefaultPlaceholderKeys.ITEM, (text, item, def) -> (text.replace(DefaultPlaceholderKeys.ITEM, item.asComponent().hoverEvent(HoverEvent.showItem(Key.key(ItemTypes.registry().findValueKey(item.type()).map(key -> key.asString()).orElse("air")), item.quantity())))));
		Placeholders.register(BlockState.class, DefaultPlaceholderKeys.BLOCK, (text, block, def) -> (text.replace(DefaultPlaceholderKeys.BLOCK, Component.text("[").append(block.type().item().map(item -> item.asComponent().hoverEvent(HoverEvent.showItem(Key.key(ItemTypes.registry().findValueKey(item).map(key -> key.asString()).orElse("air")), 1))).orElse(block.type().asComponent())).append(Component.text("]")))));
	}

}